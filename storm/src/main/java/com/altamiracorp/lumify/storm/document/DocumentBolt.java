package com.altamiracorp.lumify.storm.document;

import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.tuple.Tuple;
import com.altamiracorp.lumify.contentTypeExtraction.ContentTypeExtractor;
import com.altamiracorp.lumify.model.graph.GraphVertex;
import com.altamiracorp.lumify.storm.BaseLumifyBolt;
import com.altamiracorp.lumify.textExtraction.ArtifactExtractedInfo;
import com.altamiracorp.lumify.ucd.artifact.Artifact;
import com.altamiracorp.lumify.util.ThreadedInputStreamProcess;
import com.altamiracorp.lumify.util.ThreadedTeeInputStreamWorker;
import com.google.inject.Inject;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.hadoop.fs.Path;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DocumentBolt extends BaseLumifyBolt {
    private static final Logger LOGGER = LoggerFactory.getLogger(DocumentBolt.class.getName());
    private ThreadedInputStreamProcess<ArtifactExtractedInfo, AdditionalWorkData> threadedInputStreamProcess;
    private ContentTypeExtractor contentTypeExtractor;

    @Override
    public void prepare(Map stormConf, TopologyContext context, OutputCollector collector) {
        super.prepare(stormConf, context, collector);
        List<ThreadedTeeInputStreamWorker<ArtifactExtractedInfo, AdditionalWorkData>> workers = new ArrayList<ThreadedTeeInputStreamWorker<ArtifactExtractedInfo, AdditionalWorkData>>();
        workers.add(inject(new TextExtractorWorker()));
        workers.add(inject(new HashCalculationWorker()));
        threadedInputStreamProcess = new ThreadedInputStreamProcess<ArtifactExtractedInfo, AdditionalWorkData>("documentBoltWorkers", workers);

        try {
            mkdir("/tmp");
            mkdir("/lumify");
            mkdir("/lumify/artifacts");
            mkdir("/lumify/artifacts/text");
            mkdir("/lumify/artifacts/raw");
        } catch (IOException e) {
            collector.reportError(e);
        }
    }

    @Override
    public void cleanup() {
        this.threadedInputStreamProcess.stop();
        super.cleanup();
    }

    @Override
    protected void safeExecute(Tuple input) throws Exception {
        FileMetadata fileMetadata = getFileMetadata(input);
        LOGGER.info("processing: " + fileMetadata.getFileName() + " (mimeType: " + fileMetadata.getMimeType() + ")");

        ArtifactExtractedInfo artifactExtractedInfo = new ArtifactExtractedInfo();
        artifactExtractedInfo.setOntologyClassUri("http://altamiracorp.com/lumify#document");
        artifactExtractedInfo.setTitle(FilenameUtils.getName(fileMetadata.getFileName()));

        runWorkers(fileMetadata, artifactExtractedInfo);

        String newRawArtifactHdfsPath = moveRawFile(fileMetadata.getFileName(), artifactExtractedInfo.getRowKey());
        artifactExtractedInfo.setRawHdfsPath(newRawArtifactHdfsPath);

        if (artifactExtractedInfo.getTextRowKey() != null && artifactExtractedInfo.getTextHdfsPath() != null) {
            String newTextPath = moveTempTextFile(artifactExtractedInfo.getTextHdfsPath(), artifactExtractedInfo.getRowKey());
            artifactExtractedInfo.setTextHdfsPath(newTextPath);
        }

        GraphVertex graphVertex = addArtifact(artifactExtractedInfo);

        pushOnTextQueue(graphVertex);

        getCollector().ack(input);
    }

    private void pushOnTextQueue(GraphVertex graphVertex) {
        JSONObject textQueueDataJson = new JSONObject();
        textQueueDataJson.put("graphVertexId", graphVertex.getId());
        pushOnQueue("text", textQueueDataJson);
    }

    private FileMetadata getFileMetadata(Tuple input) throws Exception {
        String fileName = input.getString(0);
        String mimeType = null;

        if (fileName.startsWith("{")) {
            JSONObject json = getJsonFromTuple(input);
            fileName = json.optString("fileName");
            mimeType = json.optString("mimeType");
            if (fileName == null) {
                throw new RuntimeException("Expected 'fileName' in JSON document but got.\n" + json.toString());
            }
        }
        if (mimeType == null) {
            mimeType = getMimeType(fileName);
        }

        return new FileMetadata(fileName, mimeType);
    }

    private void runWorkers(FileMetadata fileMetadata, ArtifactExtractedInfo artifactExtractedInfo) throws Exception {
        InputStream in = getInputStream(fileMetadata.getFileName(), artifactExtractedInfo);
        AdditionalWorkData additionalWorkData = new AdditionalWorkData();
        additionalWorkData.setFileName(fileMetadata.getFileName());
        additionalWorkData.setMimeType(fileMetadata.getMimeType());
        additionalWorkData.setHdfsFileSystem(getHdfsFileSystem());
        try {
            List<ThreadedTeeInputStreamWorker.WorkResult<ArtifactExtractedInfo>> results = threadedInputStreamProcess.doWork(in, additionalWorkData);
            mergeResults(artifactExtractedInfo, results);
            if (LOGGER.isInfoEnabled()) {
                LOGGER.info("Extracted document info:\n" + artifactExtractedInfo.toJson().toString(2));
            }
        } finally {
            in.close();
        }
    }

    private String moveRawFile(String fileName, String rowKey) throws IOException {
        String rawArtifactHdfsPath = "/lumify/artifacts/raw/" + rowKey;
        moveFile(fileName, rawArtifactHdfsPath);
        return rawArtifactHdfsPath;
    }

    private String moveTempTextFile(String fileName, String rowKey) throws IOException {
        String newPath = "/lumify/artifacts/text/" + rowKey;
        LOGGER.info("Moving file " + fileName + " -> " + newPath);
        getHdfsFileSystem().delete(new Path(newPath), false);
        getHdfsFileSystem().rename(new Path(fileName), new Path(newPath));
        return newPath;
    }

    private String getMimeType(String fileName) throws Exception {
        InputStream in = getInputStream(fileName, null);
        return this.contentTypeExtractor.extract(in, FilenameUtils.getExtension(fileName));
    }

    private void mergeResults(ArtifactExtractedInfo artifactExtractedInfo, List<ThreadedTeeInputStreamWorker.WorkResult<ArtifactExtractedInfo>> results) throws Exception {
        for (ThreadedTeeInputStreamWorker.WorkResult<ArtifactExtractedInfo> result : results) {
            if (result.getError() != null) {
                throw result.getError();
            }
            artifactExtractedInfo.mergeFrom(result.getResult());
        }
    }

    private InputStream getInputStream(String fileName, ArtifactExtractedInfo artifactExtractedInfo) throws Exception {
        InputStream in;
        if (getFileSize(fileName) < Artifact.MAX_SIZE_OF_INLINE_FILE) {
            InputStream rawIn = openFile(fileName);
            byte[] data;
            try {
                data = IOUtils.toByteArray(rawIn);
                if (artifactExtractedInfo != null) {
                    artifactExtractedInfo.setRaw(data);
                }
            } finally {
                rawIn.close();
            }
            in = new ByteArrayInputStream(data);
        } else {
            in = openFile(fileName);
        }
        return in;
    }

    @Inject
    public void setContentTypeExtractor(ContentTypeExtractor contentTypeExtractor) {
        this.contentTypeExtractor = contentTypeExtractor;
    }
}
