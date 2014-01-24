package com.altamiracorp.lumify.storm.term.extraction;

import com.altamiracorp.lumify.core.ingest.term.extraction.TermExtractionAdditionalWorkData;
import com.altamiracorp.lumify.core.ingest.term.extraction.TermExtractionResult;
import com.altamiracorp.lumify.core.ingest.term.extraction.TermExtractionWorker;
import com.altamiracorp.lumify.core.user.User;
import com.google.inject.Inject;
import java.io.InputStream;
import java.util.Map;

public class DocumentMappingEntityExtractorWorker extends TermExtractionWorker {
    private DocumentMappingEntityExtractor documentMappingEntityExtractor;
    private User user;

    @Override
    protected TermExtractionResult doWork(InputStream work, TermExtractionAdditionalWorkData termExtractionAdditionalWorkData) throws Exception {
        return documentMappingEntityExtractor.extract(termExtractionAdditionalWorkData.getVertex(), user);
    }

    @Override
    public void prepare(Map conf, User user) throws Exception {
        this.user = user;
    }

    @Inject
    public void setDocumentMappingEntityExtractor(final DocumentMappingEntityExtractor extractor) {
        this.documentMappingEntityExtractor = extractor;
    }
}
