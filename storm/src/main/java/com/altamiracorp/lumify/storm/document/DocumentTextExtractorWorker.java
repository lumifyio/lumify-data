package com.altamiracorp.lumify.storm.document;

import com.altamiracorp.lumify.core.ingest.AdditionalArtifactWorkData;
import com.altamiracorp.lumify.core.ingest.ArtifactExtractedInfo;
import com.altamiracorp.lumify.core.ingest.document.DocumentTextExtractionWorker;
import com.altamiracorp.lumify.core.user.User;
import com.altamiracorp.lumify.core.util.HdfsLimitOutputStream;
import com.altamiracorp.lumify.core.util.ThreadedTeeInputStreamWorker;
import com.altamiracorp.lumify.textExtraction.TikaTextExtractor;
import com.altamiracorp.lumify.core.model.artifact.Artifact;
import com.google.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.Map;

public class DocumentTextExtractorWorker extends ThreadedTeeInputStreamWorker<ArtifactExtractedInfo, AdditionalArtifactWorkData> implements DocumentTextExtractionWorker {
    private static final Logger LOGGER = LoggerFactory.getLogger(DocumentTextExtractorWorker.class.getName());
    private TikaTextExtractor tikaTextExtractor;

    @Override
    protected ArtifactExtractedInfo doWork(InputStream work, AdditionalArtifactWorkData data) throws Exception {
        LOGGER.debug("Extracting document text [DocumentTextExtractorWorker]: " + data.getFileName());
        HdfsLimitOutputStream textOut = new HdfsLimitOutputStream(data.getHdfsFileSystem(), Artifact.MAX_SIZE_OF_INLINE_FILE);
        ArtifactExtractedInfo info;
        try {
            info = tikaTextExtractor.extract(work, data.getMimeType(), textOut);
        } finally {
            textOut.close();
        }
        info.setTextRowKey(textOut.getRowKey());
        if (textOut.hasExceededSizeLimit()) {
            info.setTextHdfsPath(textOut.getHdfsPath().toString());
        } else {
            LOGGER.info("extract text size: " + textOut.getSmall().length);
            info.setText(new String(textOut.getSmall()));
        }
        LOGGER.debug("Finished [DocumentTextExtractorWorker]: " + data.getFileName());
        return info;
    }

    @Inject
    public void setTikaTextExtractor(TikaTextExtractor tikaTextExtractor) {
        this.tikaTextExtractor = tikaTextExtractor;
    }

    @Override
    public void prepare(Map stormConf, User user) {
    }
}
