package com.altamiracorp.lumify.storm.termExtraction;

import com.altamiracorp.lumify.core.user.User;
import com.altamiracorp.lumify.entityExtraction.KnownEntityExtractor;
import com.altamiracorp.lumify.entityExtraction.TextExtractedInfo;
import com.altamiracorp.lumify.util.ThreadedTeeInputStreamWorker;
import com.google.inject.Inject;
import org.apache.hadoop.conf.Configuration;

import java.io.IOException;
import java.io.InputStream;

class KnownEntityExtractorWorker extends ThreadedTeeInputStreamWorker<TextExtractedInfo, TextExtractedAdditionalWorkData> {
    private KnownEntityExtractor knownEntityExtractor;

    @Override
    protected TextExtractedInfo doWork(InputStream work, TextExtractedAdditionalWorkData textExtractedAdditionalWorkData) throws Exception {
        return knownEntityExtractor.extract(work);
    }

    public KnownEntityExtractorWorker prepare(Configuration configuration, User user) throws IOException {
        knownEntityExtractor.prepare(configuration, user);
        return this;
    }

    @Inject
    void setKnownEntityExtractor(KnownEntityExtractor knownEntityExtractor) {
        this.knownEntityExtractor = knownEntityExtractor;
    }
}
