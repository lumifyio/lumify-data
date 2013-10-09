package com.altamiracorp.lumify.storm.termExtraction;

import com.altamiracorp.lumify.core.user.User;
import com.altamiracorp.lumify.entityExtraction.OpenNlpDictionaryEntityExtractor;
import com.altamiracorp.lumify.entityExtraction.TextExtractedInfo;
import com.altamiracorp.lumify.core.util.ThreadedTeeInputStreamWorker;
import com.google.inject.Inject;
import org.apache.hadoop.conf.Configuration;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;

class OpenNlpDictionaryEntityExtractorWorker extends ThreadedTeeInputStreamWorker<TextExtractedInfo, TextExtractedAdditionalWorkData> {
    private OpenNlpDictionaryEntityExtractor openNlpDictionaryEntityExtractor;

    public OpenNlpDictionaryEntityExtractorWorker() throws Exception {
    }

    @Override
    protected TextExtractedInfo doWork(InputStream work, TextExtractedAdditionalWorkData textExtractedAdditionalWorkData) throws Exception {
        return openNlpDictionaryEntityExtractor.extract(work);
    }

    public OpenNlpDictionaryEntityExtractorWorker prepare(Configuration configuration, User user) throws InterruptedException, IOException, URISyntaxException {
        openNlpDictionaryEntityExtractor.prepare(configuration, user);
        return this;
    }

    @Inject
    void setOpenNlpDictionaryEntityExtractor(OpenNlpDictionaryEntityExtractor openNlpDictionaryEntityExtractor) {
        this.openNlpDictionaryEntityExtractor = openNlpDictionaryEntityExtractor;
    }
}
