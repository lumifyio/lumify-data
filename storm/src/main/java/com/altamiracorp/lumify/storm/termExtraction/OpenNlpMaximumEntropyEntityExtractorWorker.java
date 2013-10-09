package com.altamiracorp.lumify.storm.termExtraction;

import com.altamiracorp.lumify.config.ConfigurationHelper;
import com.altamiracorp.lumify.core.ingest.termExtraction.TermExtractionWorker;
import com.altamiracorp.lumify.core.ingest.termExtraction.TextExtractedAdditionalWorkData;
import com.altamiracorp.lumify.core.ingest.termExtraction.TextExtractedInfo;
import com.altamiracorp.lumify.core.user.User;
import com.altamiracorp.lumify.entityExtraction.OpenNlpMaximumEntropyEntityExtractor;
import com.google.inject.Inject;
import org.apache.hadoop.conf.Configuration;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.Map;

public class OpenNlpMaximumEntropyEntityExtractorWorker extends TermExtractionWorker {
    private OpenNlpMaximumEntropyEntityExtractor openNlpMaximumEntropyEntityExtractor;

    @Override
    protected TextExtractedInfo doWork(InputStream work, TextExtractedAdditionalWorkData textExtractedAdditionalWorkData) throws Exception {
        return openNlpMaximumEntropyEntityExtractor.extract(work);
    }

    public void prepare(Map conf, User user) throws InterruptedException, IOException, URISyntaxException {
        Configuration configuration = ConfigurationHelper.createHadoopConfigurationFromMap(conf);

        openNlpMaximumEntropyEntityExtractor.prepare(configuration, user);
    }

    @Inject
    void setOpenNlpMaximumEntropyEntityExtractor(OpenNlpMaximumEntropyEntityExtractor openNlpMaximumEntropyEntityExtractor) {
        this.openNlpMaximumEntropyEntityExtractor = openNlpMaximumEntropyEntityExtractor;
    }
}
