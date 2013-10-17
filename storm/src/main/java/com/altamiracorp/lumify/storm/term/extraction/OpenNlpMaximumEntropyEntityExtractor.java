package com.altamiracorp.lumify.storm.term.extraction;

import opennlp.tools.namefind.NameFinderME;
import opennlp.tools.namefind.TokenNameFinder;
import opennlp.tools.namefind.TokenNameFinderModel;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class OpenNlpMaximumEntropyEntityExtractor extends OpenNlpEntityExtractor {
    @Override
    protected List<TokenNameFinder> loadFinders(String pathPrefix, FileSystem fs)
            throws IOException {
        Path finderHdfsPaths[] = {
                new Path(pathPrefix + "/en-ner-location.bin"),
                new Path(pathPrefix + "/en-ner-organization.bin"),
                new Path(pathPrefix + "/en-ner-person.bin")};
        List<TokenNameFinder> finders = new ArrayList<TokenNameFinder>();
        for (Path finderHdfsPath : finderHdfsPaths) {
            InputStream finderModelInputStream = fs.open(finderHdfsPath);
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
