package com.altamiracorp.lumify.storm.termExtraction;

import com.altamiracorp.lumify.core.user.User;
import com.altamiracorp.lumify.entityExtraction.KnownEntityExtractor;
import com.altamiracorp.lumify.entityExtraction.TextExtractedInfo;
import com.altamiracorp.lumify.util.ThreadedTeeInputStreamWorker;
import com.google.inject.Injector;
import org.apache.hadoop.conf.Configuration;

import java.io.IOException;
import java.io.InputStream;

class KnownEntityExtractorWorker extends ThreadedTeeInputStreamWorker<TextExtractedInfo, TextExtractedAdditionalWorkData> {
    private KnownEntityExtractor knownEntityExtractor;

    public KnownEntityExtractorWorker(Configuration configuration, Injector injector, User user) throws IOException {
        knownEntityExtractor = new KnownEntityExtractor(configuration, user);
        injector.injectMembers(knownEntityExtractor);
        knownEntityExtractor.init();
    }

    @Override
    protected TextExtractedInfo doWork(InputStream work, TextExtractedAdditionalWorkData textExtractedAdditionalWorkData) throws Exception {
        return knownEntityExtractor.extract(work);
    }
}
