package com.altamiracorp.lumify.entityExtraction;

import com.altamiracorp.lumify.core.user.User;
import opennlp.tools.namefind.NameFinderME;
import opennlp.tools.namefind.TokenNameFinder;
import opennlp.tools.namefind.TokenNameFinderModel;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class OpenNlpMaximumEntropyEntityExtractor extends OpenNlpEntityExtractor {

    private static final String MODEL = "OpenNlpMaximumEntropy";

    public OpenNlpMaximumEntropyEntityExtractor(Configuration configuration, User user) throws IOException {
        super(configuration, user);
    }

    @Override
    protected List<TokenNameFinder> loadFinders()
            throws IOException {
        Path finderHdfsPaths[] = {
                new Path(getPathPrefix() + "/conf/opennlp/en-ner-location.bin"),
                new Path(getPathPrefix() + "/conf/opennlp/en-ner-organization.bin"),
                new Path(getPathPrefix() + "/conf/opennlp/en-ner-person.bin")};
        List<TokenNameFinder> finders = new ArrayList<TokenNameFinder>();
        for (Path finderHdfsPath : finderHdfsPaths) {
            InputStream finderModelInputStream = getFS().open(finderHdfsPath);
            TokenNameFinderModel model = null;
            try {
                model = new TokenNameFinderModel(finderModelInputStream);
            } finally {
                finderModelInputStream.close();
            }
            NameFinderME finder = new NameFinderME(model);
            finders.add(finder);
        }

        return finders;
    }
}
