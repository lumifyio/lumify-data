package com.altamiracorp.lumify.storm.term.extraction;

import java.io.InputStream;
import java.util.Map;

import org.apache.hadoop.conf.Configuration;

import com.altamiracorp.lumify.core.config.ConfigurationHelper;
import com.altamiracorp.lumify.core.ingest.term.extraction.TermExtractionAdditionalWorkData;
import com.altamiracorp.lumify.core.ingest.term.extraction.TermExtractionResult;
import com.altamiracorp.lumify.core.ingest.term.extraction.TermExtractionWorker;
import com.altamiracorp.lumify.core.user.User;
import com.google.inject.Inject;

public class EmailAddressExtractorWorker extends TermExtractionWorker {
    private RegexEntityExtractor regExExtractor;
    private static final String EMAIL_REG_EX = "(?i)\\b[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,4}\\b";
    private static final String EMAIL_TYPE = "http://lumify.io/dev#emailAddress";

    @Override
    public void prepare(Map conf, User user) throws Exception {
        Configuration configuration = ConfigurationHelper.createHadoopConfigurationFromMap(conf);
        configuration.set(RegexEntityExtractor.REGULAR_EXPRESSION, EMAIL_REG_EX);
        configuration.set(RegexEntityExtractor.ENTITY_TYPE, EMAIL_TYPE);

        regExExtractor.prepare(configuration, user);
    }

    @Override
    protected TermExtractionResult doWork(InputStream work, TermExtractionAdditionalWorkData data) throws Exception {
        return regExExtractor.extract(work);
    }

    @Inject
    void setRegexEntityExtractor(RegexEntityExtractor extractor) {
        regExExtractor = extractor;
    }
}
