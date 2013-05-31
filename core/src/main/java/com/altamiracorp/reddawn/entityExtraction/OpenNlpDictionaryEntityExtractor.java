package com.altamiracorp.reddawn.entityExtraction;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import opennlp.tools.dictionary.Dictionary;
import opennlp.tools.namefind.DictionaryNameFinder;
import opennlp.tools.namefind.TokenNameFinder;

public class OpenNlpDictionaryEntityExtractor extends OpenNlpEntityExtractor {

	private static final String MODEL = "OpenNlpDictionary";

	private OpenNlpDictionaryRegistry dictionaryRegistry;

	@Override
	protected List<TokenNameFinder> loadFinders() throws IOException {
		List<TokenNameFinder> finders = new ArrayList<TokenNameFinder>();
		for (Entry<String, Dictionary> dictionaryEntry : getDictionaryRegistry()
				.getAllDictionaries()) {
			finders.add(new DictionaryNameFinder(dictionaryEntry.getValue(),
					dictionaryEntry.getKey()));
		}
		return finders;
	}

	@Override
	protected String getModelName() {
		return MODEL;
	}

	private OpenNlpDictionaryRegistry getDictionaryRegistry()
			throws IOException {
		if (dictionaryRegistry == null) {
			dictionaryRegistry = new OpenNlpDictionaryRegistry();
			dictionaryRegistry.loadRegistry(getPathPrefix(), getFS());
		}

		return dictionaryRegistry;
	}
}
