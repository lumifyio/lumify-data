package com.altamiracorp.lumify.storm.term.extraction;

import java.io.InputStream;
import java.util.Map;

import org.apache.hadoop.conf.Configuration;

import com.altamiracorp.lumify.config.ConfigurationHelper;
import com.altamiracorp.lumify.core.ingest.term.extraction.TermExtractionAdditionalWorkData;
import com.altamiracorp.lumify.core.ingest.term.extraction.TermExtractionResult;
import com.altamiracorp.lumify.core.ingest.term.extraction.TermExtractionWorker;
import com.altamiracorp.lumify.core.user.User;
import com.altamiracorp.lumify.entityExtraction.PhoneNumberExtractor;
import com.google.inject.Inject;

public class PhoneNumberExtractorWorker extends TermExtractionWorker {
    private PhoneNumberExtractor phoneNumberExtractor;


    @Override
    public void prepare(Map conf, User user) throws Exception {
        Configuration configuration = ConfigurationHelper.createHadoopConfigurationFromMap(conf);
        phoneNumberExtractor.prepare(configuration, user);
    }

    @Override
    protected TermExtractionResult doWork(InputStream work, TermExtractionAdditionalWorkData data) throws Exception {
        return phoneNumberExtractor.extract(work);
    }

    @Inject
    void setPhoneNumberExtractor(PhoneNumberExtractor extractor) {
        phoneNumberExtractor = extractor;
    }
}
