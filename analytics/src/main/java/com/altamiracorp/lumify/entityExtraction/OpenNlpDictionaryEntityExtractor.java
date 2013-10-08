package com.altamiracorp.lumify.entityExtraction;

import com.altamiracorp.lumify.model.dictionary.DictionaryEntry;
import com.altamiracorp.lumify.model.dictionary.DictionaryEntryRepository;
import com.google.inject.Inject;
import opennlp.tools.dictionary.Dictionary;
import opennlp.tools.namefind.DictionaryNameFinder;
import opennlp.tools.namefind.TokenNameFinder;
import opennlp.tools.util.StringList;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class OpenNlpDictionaryEntityExtractor extends OpenNlpEntityExtractor {

    private DictionaryEntryRepository dictionaryEntryRepository;

    @Override
    protected List<TokenNameFinder> loadFinders() throws IOException {
        List<TokenNameFinder> finders = new ArrayList<TokenNameFinder>();
        for (Entry<String, Dictionary> dictionaryEntry : getDictionaries().entrySet()) {
            finders.add(new DictionaryNameFinder(dictionaryEntry.getValue(), dictionaryEntry.getKey()));
        }
        return finders;
    }

    private Map<String, Dictionary> getDictionaries() {
        Map<String, Dictionary> dictionaries = new HashMap<String, Dictionary>();
        List<DictionaryEntry> entries = dictionaryEntryRepository.findAll(getUser());
        for (DictionaryEntry entry : entries) {

            if (!dictionaries.containsKey(entry.getMetadata().getConcept())) {
                dictionaries.put(entry.getMetadata().getConcept(), new Dictionary());
            }

            dictionaries.get(entry.getMetadata().getConcept()).put(tokensToStringList(entry.getMetadata().getTokens()));
        }

        return dictionaries;
    }

    @Inject
    public void setDictionaryEntryRepository(DictionaryEntryRepository dictionaryEntryRepository) {
        this.dictionaryEntryRepository = dictionaryEntryRepository;
    }

    private StringList tokensToStringList(String tokens) {
        return new StringList(tokens.split(" "));
    }
}
