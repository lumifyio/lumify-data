package com.altamiracorp.lumify.storm;

import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.tuple.Tuple;
import com.altamiracorp.lumify.model.RowKeyHelper;
import com.altamiracorp.lumify.model.graph.GraphVertex;
import com.altamiracorp.lumify.textExtraction.ArtifactExtractedInfo;
import com.altamiracorp.lumify.textExtraction.TikaTextExtractor;
import com.altamiracorp.lumify.ucd.artifact.Artifact;
import com.altamiracorp.lumify.util.HdfsLimitOutputStream;
import com.altamiracorp.lumify.util.ThreadedInputStreamProcess;
import com.altamiracorp.lumify.util.ThreadedTeeInputStreamWorker;
import com.google.inject.Inject;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.hadoop.fs.FileSystem;
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
    private TikaTextExtractor tikaTextExtractor;
    private ThreadedInputStreamProcess<ArtifactExtractedInfo, AdditionalWorkData> threadedInputStreamProcess;

    @Override
    public void prepare(Map stormConf, TopologyContext context, OutputCollector collector) {
        super.prepare(stormConf, context, collector);
        List<ThreadedTeeInputStreamWorker<ArtifactExtractedInfo, AdditionalWorkData>> workers = new ArrayList<ThreadedTeeInputStreamWorker<ArtifactExtractedInfo, AdditionalWorkData>>();
        workers.add(new TextExtractorWorker());
        workers.add(new HashCalculationWorker());
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

        LOGGER.info("processing: " + fileName + " (mimeType: " + mimeType + ")");

        ArtifactExtractedInfo artifactExtractedInfo = new ArtifactExtractedInfo();
        artifactExtractedInfo.setOntologyClassUri("http://altamiracorp.com/lumify#document");
        artifactExtractedInfo.setTitle(FilenameUtils.getName(fileName));

        InputStream in = getInputStream(fileName, artifactExtractedInfo);
        AdditionalWorkData additionalWorkData = new AdditionalWorkData();
        additionalWorkData.setFileName(fileName);
        additionalWorkData.setMimeType(mimeType);
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

        String rawArtifactHdfsPath = "/lumify/artifacts/raw/" + artifactExtractedInfo.getRowKey();
        moveFile(fileName, rawArtifactHdfsPath);
        artifactExtractedInfo.setRawHdfsPath(rawArtifactHdfsPath);

        GraphVertex graphVertex = addArtifact(artifactExtractedInfo);
        JSONObject textQueueDataJson = new JSONObject();
        textQueueDataJson.put("graphVertexId", graphVertex.getId());
        pushOnQueue("text", textQueueDataJson);

        getCollector().ack(input);
    }

    // TODO: need a better check for mime type
    private String getMimeType(String fileName) {
        String ext = FilenameUtils.getExtension(fileName).toLowerCase();
        if (ext.equals("html") || ext.equals("htm")) {
            return "text/html";
        }
        return "binary";
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
                artifactExtractedInfo.setRaw(data);
            } finally {
                rawIn.close();
            }
            in = new ByteArrayInputStream(data);
        } else {
            in = openFile(fileName);
        }
        return in;
    }

    private class AdditionalWorkData {

        private String mimeType;
        private String fileName;
        private FileSystem hdfsFileSystem;

        public void setMimeType(String mimeType) {
            this.mimeType = mimeType;
        }

        public String getMimeType() {
            return mimeType;
        }

        public void setFileName(String fileName) {
            this.fileName = fileName;
        }

        public String getFileName() {
            return fileName;
        }

        public FileSystem getHdfsFileSystem() {
            return hdfsFileSystem;
        }

        public void setHdfsFileSystem(FileSystem hdfsFileSystem) {
            this.hdfsFileSystem = hdfsFileSystem;
        }
    }

    @Inject
    public void setTikaTextExtractor(TikaTextExtractor tikaTextExtractor) {
        this.tikaTextExtractor = tikaTextExtractor;
    }

    private class TextExtractorWorker extends ThreadedTeeInputStreamWorker<ArtifactExtractedInfo, AdditionalWorkData> {
        @Override
        protected ArtifactExtractedInfo doWork(InputStream work, AdditionalWorkData data) throws Exception {
            HdfsLimitOutputStream textOut = new HdfsLimitOutputStream(data.getHdfsFileSystem(), Artifact.MAX_SIZE_OF_INLINE_FILE);
            ArtifactExtractedInfo info;
            try {
                info = tikaTextExtractor.extract(work, data.getMimeType(), textOut);
            } finally {
                textOut.close();
            }
            if (textOut.hasExceededSizeLimit()) {
                String newPath = "/lumify/artifacts/text/" + textOut.getRowKey();
                getHdfsFileSystem().delete(new Path(newPath), false);
                getHdfsFileSystem().rename(new Path(textOut.getHdfsPath().toString()), new Path(newPath));
                info.setTextHdfsPath(newPath);
            } else {
                info.setText(new String(textOut.getSmall()));
            }
            return info;
        }

        @Override
        public String getName() {
            return "textExtractor";
        }
    }

    private class HashCalculationWorker extends ThreadedTeeInputStreamWorker<ArtifactExtractedInfo, AdditionalWorkData> {
        @Override
        protected ArtifactExtractedInfo doWork(InputStream work, AdditionalWorkData additionalWorkData) throws Exception {
            ArtifactExtractedInfo info = new ArtifactExtractedInfo();
            info.setRowKey(RowKeyHelper.buildSHA256KeyString(work));
            return info;
        }

        @Override
        public String getName() {
            return "hashCalculator";
        }
    }
}
