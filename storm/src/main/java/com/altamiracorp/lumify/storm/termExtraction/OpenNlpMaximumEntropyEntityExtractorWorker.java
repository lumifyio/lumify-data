package com.altamiracorp.lumify.storm.termExtraction;

import com.altamiracorp.lumify.core.user.User;
import com.altamiracorp.lumify.entityExtraction.OpenNlpMaximumEntropyEntityExtractor;
import com.altamiracorp.lumify.entityExtraction.TextExtractedInfo;
import com.altamiracorp.lumify.util.ThreadedTeeInputStreamWorker;
import com.google.inject.Inject;
import org.apache.hadoop.conf.Configuration;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;

class OpenNlpMaximumEntropyEntityExtractorWorker extends ThreadedTeeInputStreamWorker<TextExtractedInfo, TextExtractedAdditionalWorkData> {
    private OpenNlpMaximumEntropyEntityExtractor openNlpMaximumEntropyEntityExtractor;

    @Override
    protected TextExtractedInfo doWork(InputStream work, TextExtractedAdditionalWorkData textExtractedAdditionalWorkData) throws Exception {
        return openNlpMaximumEntropyEntityExtractor.extract(work);
    }

    public OpenNlpMaximumEntropyEntityExtractorWorker prepare(Configuration configuration, User user) throws InterruptedException, IOException, URISyntaxException {
        openNlpMaximumEntropyEntityExtractor.prepare(configuration, user);
        return this;
    }

    @Inject
    void setOpenNlpMaximumEntropyEntityExtractor(OpenNlpMaximumEntropyEntityExtractor openNlpMaximumEntropyEntityExtractor) {
        this.openNlpMaximumEntropyEntityExtractor = openNlpMaximumEntropyEntityExtractor;
    }
}
