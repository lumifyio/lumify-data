package com.altamiracorp.lumify.entityExtraction;

import opennlp.tools.dictionary.Dictionary;
import opennlp.tools.namefind.DictionaryNameFinder;
import opennlp.tools.namefind.TokenNameFinder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

public class OpenNlpDictionaryEntityExtractor extends OpenNlpEntityExtractor {
    private OpenNlpDictionaryRegistry dictionaryRegistry;

    @Override
    protected List<TokenNameFinder> loadFinders() throws IOException {
        List<TokenNameFinder> finders = new ArrayList<TokenNameFinder>();
        for (Entry<String, Dictionary> dictionaryEntry : getDictionaryRegistry().getAllDictionaries()) {
            finders.add(new DictionaryNameFinder(dictionaryEntry.getValue(), dictionaryEntry.getKey()));
        }
        return finders;
    }

    private OpenNlpDictionaryRegistry getDictionaryRegistry() throws IOException {
        if (dictionaryRegistry == null) {
            dictionaryRegistry = new OpenNlpDictionaryRegistry();
            dictionaryRegistry.loadRegistry(getPathPrefix(), getFS());
        }

        return dictionaryRegistry;
    }
}
