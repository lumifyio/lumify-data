package com.altamiracorp.lumify.storm.termExtraction;

import com.altamiracorp.lumify.core.user.User;
import com.altamiracorp.lumify.entityExtraction.OpenNlpDictionaryEntityExtractor;
import com.altamiracorp.lumify.entityExtraction.TextExtractedInfo;
import com.altamiracorp.lumify.util.ThreadedTeeInputStreamWorker;
import com.google.inject.Injector;
import org.apache.hadoop.conf.Configuration;

import java.io.InputStream;

class OpenNlpDictionaryEntityExtractorWorker extends ThreadedTeeInputStreamWorker<TextExtractedInfo, TextExtractedAdditionalWorkData> {
    private OpenNlpDictionaryEntityExtractor openNlpDictionaryEntityExtractor;

    public OpenNlpDictionaryEntityExtractorWorker(Configuration configuration, Injector injector, User user) throws Exception {
        openNlpDictionaryEntityExtractor = new OpenNlpDictionaryEntityExtractor(configuration, user);
        injector.injectMembers(openNlpDictionaryEntityExtractor);
        openNlpDictionaryEntityExtractor.init();
    }

    @Override
    protected TextExtractedInfo doWork(InputStream work, TextExtractedAdditionalWorkData textExtractedAdditionalWorkData) throws Exception {
        return openNlpDictionaryEntityExtractor.extract(work);
    }
}
