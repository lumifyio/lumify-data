package com.altamiracorp.lumify.storm.document;

import com.altamiracorp.lumify.core.ingest.AdditionalArtifactWorkData;
import com.altamiracorp.lumify.core.ingest.ArtifactExtractedInfo;
import com.altamiracorp.lumify.core.ingest.TextExtractionWorkerPrepareData;
import com.altamiracorp.lumify.core.ingest.document.DocumentTextExtractionWorker;
import com.altamiracorp.lumify.core.model.artifact.Artifact;
import com.altamiracorp.lumify.core.util.HdfsLimitOutputStream;
import com.altamiracorp.lumify.core.util.LumifyLogger;
import com.altamiracorp.lumify.core.util.LumifyLoggerFactory;
import com.altamiracorp.lumify.core.util.ThreadedTeeInputStreamWorker;
import com.altamiracorp.lumify.textExtraction.TikaTextExtractor;
import com.google.inject.Inject;

import java.io.InputStream;

public class DocumentTextExtractorWorker extends ThreadedTeeInputStreamWorker<ArtifactExtractedInfo, AdditionalArtifactWorkData> implements DocumentTextExtractionWorker {
    private static final LumifyLogger LOGGER = LumifyLoggerFactory.getLogger(DocumentTextExtractorWorker.class);
    private TikaTextExtractor tikaTextExtractor;

    @Override
    protected ArtifactExtractedInfo doWork(InputStream work, AdditionalArtifactWorkData data) throws Exception {
        LOGGER.debug("Extracting document text [DocumentTextExtractorWorker]: %s", data.getFileName());
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
            LOGGER.debug("extract text size: %d", textOut.getSmall().length);
            info.setText(new String(textOut.getSmall()));
        }
        LOGGER.debug("Finished [DocumentTextExtractorWorker]: %s", data.getFileName());
        return info;
    }

    @Inject
    public void setTikaTextExtractor(TikaTextExtractor tikaTextExtractor) {
        this.tikaTextExtractor = tikaTextExtractor;
    }

    @Override
    public void prepare(TextExtractionWorkerPrepareData data) {
    }
}
