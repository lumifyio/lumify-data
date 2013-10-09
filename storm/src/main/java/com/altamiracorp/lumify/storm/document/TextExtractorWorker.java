package com.altamiracorp.lumify.storm.document;

import com.altamiracorp.lumify.textExtraction.ArtifactExtractedInfo;
import com.altamiracorp.lumify.textExtraction.TikaTextExtractor;
import com.altamiracorp.lumify.ucd.artifact.Artifact;
import com.altamiracorp.lumify.core.util.HdfsLimitOutputStream;
import com.altamiracorp.lumify.core.util.ThreadedTeeInputStreamWorker;
import com.google.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;

class TextExtractorWorker extends ThreadedTeeInputStreamWorker<ArtifactExtractedInfo, AdditionalWorkData> {
    private static final Logger LOGGER = LoggerFactory.getLogger(TextExtractorWorker.class.getName());
    private TikaTextExtractor tikaTextExtractor;

    @Override
    protected ArtifactExtractedInfo doWork(InputStream work, AdditionalWorkData data) throws Exception {
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
        return info;
    }

    @Override
    public String getName() {
        return "textExtractor";
    }

    @Inject
    public void setTikaTextExtractor(TikaTextExtractor tikaTextExtractor) {
        this.tikaTextExtractor = tikaTextExtractor;
    }
}
