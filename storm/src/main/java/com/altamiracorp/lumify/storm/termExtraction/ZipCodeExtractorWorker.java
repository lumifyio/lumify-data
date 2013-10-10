package com.altamiracorp.lumify.storm.termExtraction;

import java.io.InputStream;
import java.util.Map;

import org.apache.hadoop.conf.Configuration;

import com.altamiracorp.lumify.config.ConfigurationHelper;
import com.altamiracorp.lumify.core.ingest.termExtraction.TermExtractionAdditionalWorkData;
import com.altamiracorp.lumify.core.ingest.termExtraction.TermExtractionResult;
import com.altamiracorp.lumify.core.ingest.termExtraction.TermExtractionWorker;
import com.altamiracorp.lumify.core.user.User;
import com.altamiracorp.lumify.entityExtraction.RegexEntityExtractor;
import com.google.inject.Inject;

public class ZipCodeExtractorWorker extends TermExtractionWorker {
    private RegexEntityExtractor regExExtractor;
    private static final String ZIPCODE_REG_EX = "\\b\\d{5}-\\d{4}\\b|\\b\\d{5}\\b";
    private static final String LOCATION_TYPE = "location";

    @Override
    public void prepare(Map conf, User user) throws Exception {
        Configuration configuration = ConfigurationHelper.createHadoopConfigurationFromMap(conf);
        configuration.set(RegexEntityExtractor.REGULAR_EXPRESSION, ZIPCODE_REG_EX);
        configuration.set(RegexEntityExtractor.ENTITY_TYPE, LOCATION_TYPE);

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
