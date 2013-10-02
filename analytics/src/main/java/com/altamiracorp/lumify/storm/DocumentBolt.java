package com.altamiracorp.lumify.storm;

import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.tuple.Tuple;
import com.altamiracorp.lumify.model.RowKeyHelper;
import com.altamiracorp.lumify.model.graph.GraphVertex;
import com.altamiracorp.lumify.textExtraction.ArtifactExtractedInfo;
import com.altamiracorp.lumify.textExtraction.TikaTextExtractor;
import com.altamiracorp.lumify.ucd.artifact.ArtifactType;
import com.altamiracorp.lumify.util.HdfsLimitOutputStream;
import com.altamiracorp.lumify.util.ThreadedInputStreamProcess;
import com.altamiracorp.lumify.util.ThreadedTeeInputStreamWorker;
import com.google.inject.Inject;
import org.apache.hadoop.fs.FileSystem;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DocumentBolt extends BaseLumifyBolt {
    private static final Logger LOGGER = LoggerFactory.getLogger(DocumentBolt.class.getName());
    private static final int MAX_TEXT_COLUMN_SIZE = 100000; // This is the maximum size of the text to store in the table as opposed to HDFS
    private TikaTextExtractor tikaTextExtractor;
    private ThreadedInputStreamProcess<ArtifactExtractedInfo, AdditionalWorkData> threadedInputStreamProcess;

    @Override
    public void prepare(Map stormConf, TopologyContext context, OutputCollector collector) {
        super.prepare(stormConf, context, collector);
        List<ThreadedTeeInputStreamWorker<ArtifactExtractedInfo, AdditionalWorkData>> workers = new ArrayList<ThreadedTeeInputStreamWorker<ArtifactExtractedInfo, AdditionalWorkData>>();
        workers.add(new TextExtractorWorker());
        workers.add(new HashCalculationWorker());
        threadedInputStreamProcess = new ThreadedInputStreamProcess<ArtifactExtractedInfo, AdditionalWorkData>("documentBoltWorkers", workers);
    }

    @Override
    public void cleanup() {
        this.threadedInputStreamProcess.stop();
        super.cleanup();
    }

    @Override
    protected void safeExecute(Tuple input) throws Exception {
        JSONObject json = new JSONObject(input.getString(0));
        String fileName = json.getString("fileName");
        String mimeType = json.getString("mimeType");

        LOGGER.info("processing: " + fileName + " (mimeType: " + mimeType + ")");

        ArtifactExtractedInfo artifactExtractedInfo = new ArtifactExtractedInfo();

        InputStream in = openFile(fileName);
        AdditionalWorkData additionalWorkData = new AdditionalWorkData();
        additionalWorkData.setFileName(fileName);
        additionalWorkData.setMimeType(mimeType);
        additionalWorkData.setHdfsFileSystem(getHdfsFileSystem());
        try {
            List<ThreadedTeeInputStreamWorker.WorkResult<ArtifactExtractedInfo>> results = threadedInputStreamProcess.doWork(in, additionalWorkData);
            for (ThreadedTeeInputStreamWorker.WorkResult<ArtifactExtractedInfo> result : results) {
                if (result.getError() != null) {
                    throw result.getError();
                }
                artifactExtractedInfo.mergeFrom(result.getResult());
            }
            if (LOGGER.isInfoEnabled()) {
                LOGGER.info("Extracted document info:\n" + artifactExtractedInfo.toJson().toString(2));
            }
        } finally {
            in.close();
        }

        // TODO refactor to use the hash calculated from the workers
        GraphVertex graphVertex;
        in = openFile(fileName);
        try {
            String classUri = "http://altamiracorp.com/lumify#document";
            long rawSize = getFileSize(fileName);
            graphVertex = addArtifact(rawSize, in, artifactExtractedInfo.getText(), classUri, ArtifactType.DOCUMENT);
        } finally {
            in.close();
        }

        getCollector().ack(input);
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
            HdfsLimitOutputStream textOut = new HdfsLimitOutputStream(data.getHdfsFileSystem(), MAX_TEXT_COLUMN_SIZE);
            ArtifactExtractedInfo info = tikaTextExtractor.extract(work, data.getFileName(), data.getMimeType(), textOut);
            if (textOut.hasExceededSizeLimit()) {
                info.setTextHdfsPath(textOut.getHdfsPath().toString()); // TODO: we should move this file to a known MD5 location or something.
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
