package com.altamiracorp.lumify.storm.term.extraction;

import com.altamiracorp.lumify.core.config.ConfigurationHelper;
import com.altamiracorp.lumify.core.ingest.term.extraction.TermExtractionAdditionalWorkData;
import com.altamiracorp.lumify.core.ingest.term.extraction.TermExtractionResult;
import com.altamiracorp.lumify.core.ingest.term.extraction.TermExtractionWorker;
import com.altamiracorp.lumify.core.user.User;
import com.altamiracorp.lumify.storm.term.extraction.OpenNlpMaximumEntropyEntityExtractor;
import com.google.inject.Inject;
import org.apache.hadoop.conf.Configuration;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.Map;

public class OpenNlpMaximumEntropyEntityExtractorWorker extends TermExtractionWorker {
    private OpenNlpMaximumEntropyEntityExtractor openNlpMaximumEntropyEntityExtractor;

    @Override
    protected TermExtractionResult doWork(InputStream work, TermExtractionAdditionalWorkData termExtractionAdditionalWorkData) throws Exception {
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
