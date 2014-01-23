package com.altamiracorp.lumify.storm.document;

import com.altamiracorp.lumify.core.ingest.AdditionalArtifactWorkData;
import com.altamiracorp.lumify.core.ingest.ArtifactExtractedInfo;
import com.altamiracorp.lumify.core.ingest.TextExtractionWorkerPrepareData;
import com.altamiracorp.lumify.core.ingest.document.DocumentTextExtractionWorker;
import com.altamiracorp.lumify.core.util.LumifyLogger;
import com.altamiracorp.lumify.core.util.LumifyLoggerFactory;
import com.altamiracorp.lumify.core.util.ThreadedTeeInputStreamWorker;
import com.altamiracorp.lumify.textExtraction.TikaTextExtractor;
import com.google.inject.Inject;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

public class DocumentTextExtractorWorker extends ThreadedTeeInputStreamWorker<ArtifactExtractedInfo, AdditionalArtifactWorkData> implements DocumentTextExtractionWorker {
    private static final LumifyLogger LOGGER = LumifyLoggerFactory.getLogger(DocumentTextExtractorWorker.class);
    private TikaTextExtractor tikaTextExtractor;

    @Override
    protected ArtifactExtractedInfo doWork(InputStream work, AdditionalArtifactWorkData data) throws Exception {
        LOGGER.debug("Extracting document text [DocumentTextExtractorWorker]: %s", data.getFileName());
        ArtifactExtractedInfo info;
        ByteArrayOutputStream textOut = new ByteArrayOutputStream();
        try {
            info = tikaTextExtractor.extract(work, data.getMimeType(), textOut);
        } finally {
            textOut.close();
        }
        info.setText(new String(textOut.toByteArray()));
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
