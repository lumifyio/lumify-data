package com.altamiracorp.reddawn.entityExtraction;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import opennlp.tools.dictionary.Dictionary;
import opennlp.tools.namefind.DictionaryNameFinder;
import opennlp.tools.namefind.TokenNameFinder;
import opennlp.tools.tokenize.Tokenizer;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;

import org.apache.hadoop.mapreduce.Mapper.Context;

public class OpenNlpDictionaryEntityExtractor extends OpenNlpEntityExtractor {

	private static final String MODEL = "OpenNlpDictionary";

	private OpenNlpDictionaryRegistry dictionaryRegistry;

	@Override
	protected List<TokenNameFinder> getFinders(Context context)
			throws IOException {
		List<TokenNameFinder> finders = new ArrayList<TokenNameFinder>();
		for (Dictionary dictionary : getDictionaryRegistry(context)
				.getAllDictionaries()) {
			finders.add(new DictionaryNameFinder(dictionary));
		}
		return finders;
	}

	@Override
	protected String getModelName() {
		return MODEL;
	}

	private OpenNlpDictionaryRegistry getDictionaryRegistry(Context context)
			throws IOException {
		if (dictionaryRegistry == null) {
			dictionaryRegistry = new OpenNlpDictionaryRegistry();
			dictionaryRegistry.loadRegistry(getPathPrefix(), getFS());
		}

		return dictionaryRegistry;
	}
}
