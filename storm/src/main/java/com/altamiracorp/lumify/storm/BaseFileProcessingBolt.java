package com.altamiracorp.lumify.storm;


import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.tuple.Tuple;
import com.altamiracorp.lumify.contentTypeExtraction.ContentTypeExtractor;
import com.altamiracorp.lumify.core.ingest.AdditionalArtifactWorkData;
import com.altamiracorp.lumify.core.ingest.ArtifactExtractedInfo;
import com.altamiracorp.lumify.core.ingest.TextExtractionWorker;
import com.altamiracorp.lumify.core.util.ThreadedInputStreamProcess;
import com.altamiracorp.lumify.core.util.ThreadedTeeInputStreamWorker;
import com.altamiracorp.lumify.model.graph.GraphVertex;
import com.altamiracorp.lumify.storm.file.FileMetadata;
import com.altamiracorp.lumify.ucd.artifact.Artifact;
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
import java.util.ServiceLoader;

public abstract class BaseFileProcessingBolt extends BaseLumifyBolt {

    private static final Logger LOGGER = LoggerFactory.getLogger(BaseFileProcessingBolt.class);
    private ThreadedInputStreamProcess<ArtifactExtractedInfo, AdditionalArtifactWorkData> threadedInputStreamProcess;
    private ContentTypeExtractor contentTypeExtractor;

    @Override
    public void prepare(Map stormConf, TopologyContext context, OutputCollector collector) {
        super.prepare(stormConf, context, collector);
        try {
            mkdir("/tmp");
            mkdir("/lumify");
            mkdir("/lumify/artifacts");
            mkdir("/lumify/artifacts/text");
            mkdir("/lumify/artifacts/raw");
        } catch (IOException e) {
            collector.reportError(e);
        }

        List<ThreadedTeeInputStreamWorker<ArtifactExtractedInfo, AdditionalArtifactWorkData>> workers = new ArrayList<ThreadedTeeInputStreamWorker<ArtifactExtractedInfo, AdditionalArtifactWorkData>>();

        ServiceLoader services = getServiceLoader();
        for (Object service : services) {
            LOGGER.info("adding class " + service.getClass().getName() + " to " + getClass().getName());
            inject(service);
        }
        for (Object service : services) {
            ((TextExtractionWorker) service).prepare(stormConf, getUser());
        }
        for (Object service : services) {
            workers.add((ThreadedTeeInputStreamWorker<ArtifactExtractedInfo, AdditionalArtifactWorkData>) service);
        }

        setThreadedInputStreamProcess(new ThreadedInputStreamProcess<ArtifactExtractedInfo, AdditionalArtifactWorkData>(getThreadPrefix(), workers));
    }

    protected abstract String getThreadPrefix();

    protected abstract ServiceLoader getServiceLoader();

    @Override
    public void cleanup() {
        this.threadedInputStreamProcess.stop();
        super.cleanup();
    }

    protected GraphVertex processFile(Tuple input) throws Exception {
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

        return graphVertex;
    }

    protected void runWorkers(FileMetadata fileMetadata, ArtifactExtractedInfo artifactExtractedInfo) throws Exception {
        InputStream in = getInputStream(fileMetadata.getFileName(), artifactExtractedInfo);
        AdditionalArtifactWorkData additionalDocumentWorkData = new AdditionalArtifactWorkData();
        additionalDocumentWorkData.setFileName(fileMetadata.getFileName());
        additionalDocumentWorkData.setMimeType(fileMetadata.getMimeType());
        additionalDocumentWorkData.setHdfsFileSystem(getHdfsFileSystem());
        try {
            List<ThreadedTeeInputStreamWorker.WorkResult<ArtifactExtractedInfo>> results = threadedInputStreamProcess.doWork(in, additionalDocumentWorkData);
            mergeResults(artifactExtractedInfo, results);
            if (LOGGER.isInfoEnabled()) {
                LOGGER.info("Extracted document info:\n" + artifactExtractedInfo.toJson().toString(2));
            }
        } finally {
            in.close();
        }
    }

    protected void mergeResults(ArtifactExtractedInfo artifactExtractedInfo, List<ThreadedTeeInputStreamWorker.WorkResult<ArtifactExtractedInfo>> results) throws Exception {
        for (ThreadedTeeInputStreamWorker.WorkResult<ArtifactExtractedInfo> result : results) {
            if (result.getError() != null) {
                throw result.getError();
            }
            artifactExtractedInfo.mergeFrom(result.getResult());
        }
    }

    protected FileMetadata getFileMetadata(Tuple input) throws Exception {
        String fileName = input.getString(0);
        if (fileName == null || fileName.length() == 0) {
            throw new RuntimeException("Invalid item on the queue.");
        }
        String mimeType = null;

        if (fileName.startsWith("{")) {
            JSONObject json = getJsonFromTuple(input);
            fileName = json.optString("fileName");
            mimeType = json.optString("mimeType");
            if (fileName == null || fileName.length() == 0) {
                throw new RuntimeException("Expected 'fileName' in JSON document but got.\n" + json.toString());
            }
        }
        if (mimeType == null) {
            mimeType = getMimeType(fileName);
        }

        return new FileMetadata(fileName, mimeType);
    }

    protected InputStream getInputStream(String fileName, ArtifactExtractedInfo artifactExtractedInfo) throws Exception {
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

    private String getMimeType(String fileName) throws Exception {
        InputStream in = getInputStream(fileName, null);
        return this.contentTypeExtractor.extract(in, FilenameUtils.getExtension(fileName));
    }


    protected String moveRawFile(String fileName, String rowKey) throws IOException {
        String rawArtifactHdfsPath = "/lumify/artifacts/raw/" + rowKey;
        moveFile(fileName, rawArtifactHdfsPath);
        return rawArtifactHdfsPath;
    }


    protected String moveTempTextFile(String fileName, String rowKey) throws IOException {
        String newPath = "/lumify/artifacts/text/" + rowKey;
        LOGGER.info("Moving file " + fileName + " -> " + newPath);
        getHdfsFileSystem().delete(new Path(newPath), false);
        getHdfsFileSystem().rename(new Path(fileName), new Path(newPath));
        return newPath;
    }

    protected void setThreadedInputStreamProcess(ThreadedInputStreamProcess<ArtifactExtractedInfo, AdditionalArtifactWorkData> threadedInputStreamProcess) {
        this.threadedInputStreamProcess = threadedInputStreamProcess;
    }

    @Override
    public void safeExecute(Tuple input) throws Exception {
        GraphVertex graphVertex = processFile(input);
        pushOnTextQueue(graphVertex);
        getCollector().ack(input);
    }

    protected void pushOnTextQueue(GraphVertex graphVertex) {
        JSONObject textQueueDataJson = new JSONObject();
        textQueueDataJson.put("graphVertexId", graphVertex.getId());
        pushOnQueue("text", textQueueDataJson);
    }

    @Inject
    public void setContentTypeExtractor(ContentTypeExtractor contentTypeExtractor) {
        this.contentTypeExtractor = contentTypeExtractor;
    }
}
