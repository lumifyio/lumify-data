package com.altamiracorp.lumify.storm.termExtraction;

import com.altamiracorp.lumify.config.ConfigurationHelper;
import com.altamiracorp.lumify.core.ingest.termExtraction.TermExtractionAdditionalWorkData;
import com.altamiracorp.lumify.core.ingest.termExtraction.TermExtractionWorker;
import com.altamiracorp.lumify.core.ingest.termExtraction.TermExtractionResult;
import com.altamiracorp.lumify.core.user.User;
import com.altamiracorp.lumify.entityExtraction.OpenNlpDictionaryEntityExtractor;
import com.google.inject.Inject;
import org.apache.hadoop.conf.Configuration;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.Map;

public class OpenNlpDictionaryEntityExtractorWorker extends TermExtractionWorker {
    private OpenNlpDictionaryEntityExtractor openNlpDictionaryEntityExtractor;

    public OpenNlpDictionaryEntityExtractorWorker() throws Exception {
    }

    @Override
    protected TermExtractionResult doWork(InputStream work, TermExtractionAdditionalWorkData termExtractionAdditionalWorkData) throws Exception {
        return openNlpDictionaryEntityExtractor.extract(work);
    }

    public void prepare(Map conf, User user) throws InterruptedException, IOException, URISyntaxException {
        Configuration configuration = ConfigurationHelper.createHadoopConfigurationFromMap(conf);
        openNlpDictionaryEntityExtractor.prepare(configuration, user);
    }

    @Inject
    void setOpenNlpDictionaryEntityExtractor(OpenNlpDictionaryEntityExtractor openNlpDictionaryEntityExtractor) {
        this.openNlpDictionaryEntityExtractor = openNlpDictionaryEntityExtractor;
    }
}
