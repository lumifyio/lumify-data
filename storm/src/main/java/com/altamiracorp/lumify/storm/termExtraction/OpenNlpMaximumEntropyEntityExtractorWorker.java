package com.altamiracorp.lumify.storm.termExtraction;

import com.altamiracorp.lumify.core.user.User;
import com.altamiracorp.lumify.entityExtraction.OpenNlpMaximumEntropyEntityExtractor;
import com.altamiracorp.lumify.entityExtraction.TextExtractedInfo;
import com.altamiracorp.lumify.util.ThreadedTeeInputStreamWorker;
import com.google.inject.Injector;
import org.apache.hadoop.conf.Configuration;

import java.io.InputStream;

class OpenNlpMaximumEntropyEntityExtractorWorker extends ThreadedTeeInputStreamWorker<TextExtractedInfo, TextExtractedAdditionalWorkData> {
    private OpenNlpMaximumEntropyEntityExtractor openNlpMaximumEntropyEntityExtractor;

    public OpenNlpMaximumEntropyEntityExtractorWorker(Configuration configuration, Injector injector, User user) throws Exception {
        openNlpMaximumEntropyEntityExtractor = new OpenNlpMaximumEntropyEntityExtractor(configuration, user);
        injector.injectMembers(openNlpMaximumEntropyEntityExtractor);
        openNlpMaximumEntropyEntityExtractor.init();
    }

    @Override
    protected TextExtractedInfo doWork(InputStream work, TextExtractedAdditionalWorkData textExtractedAdditionalWorkData) throws Exception {
        return openNlpMaximumEntropyEntityExtractor.extract(work);
    }
}
