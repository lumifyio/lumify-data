package com.altamiracorp.lumify.storm.term.extraction;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.Map;

import org.apache.hadoop.conf.Configuration;

import com.altamiracorp.lumify.config.ConfigurationHelper;
import com.altamiracorp.lumify.core.ingest.term.extraction.TermExtractionAdditionalWorkData;
import com.altamiracorp.lumify.core.ingest.term.extraction.TermExtractionResult;
import com.altamiracorp.lumify.core.ingest.term.extraction.TermExtractionWorker;
import com.altamiracorp.lumify.core.user.User;
import com.altamiracorp.lumify.storm.term.extraction.OpenNlpDictionaryEntityExtractor;
import com.google.inject.Inject;

public class OpenNlpDictionaryEntityExtractorWorker extends TermExtractionWorker {
    private OpenNlpDictionaryEntityExtractor openNlpDictionaryEntityExtractor;

    @Override
    protected TermExtractionResult doWork(InputStream work, TermExtractionAdditionalWorkData termExtractionAdditionalWorkData) throws Exception {
        return openNlpDictionaryEntityExtractor.extract(work);
    }

    @Override
    public void prepare(Map conf, User user) throws InterruptedException, IOException, URISyntaxException {
        Configuration configuration = ConfigurationHelper.createHadoopConfigurationFromMap(conf);
        openNlpDictionaryEntityExtractor.prepare(configuration, user);
    }

    @Inject
    void setOpenNlpDictionaryEntityExtractor(OpenNlpDictionaryEntityExtractor openNlpDictionaryEntityExtractor) {
        this.openNlpDictionaryEntityExtractor = openNlpDictionaryEntityExtractor;
    }
}
