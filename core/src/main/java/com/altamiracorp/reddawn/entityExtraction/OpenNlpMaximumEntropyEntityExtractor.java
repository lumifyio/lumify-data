package com.altamiracorp.reddawn.entityExtraction;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import opennlp.tools.namefind.NameFinderME;
import opennlp.tools.namefind.TokenNameFinder;
import opennlp.tools.namefind.TokenNameFinderModel;

import org.apache.hadoop.fs.Path;

public class OpenNlpMaximumEntropyEntityExtractor extends
		OpenNlpEntityExtractor {

	private static final String MODEL = "OpenNlpMaximumEntropy";

	@Override
	protected List<TokenNameFinder> loadFinders()
			throws IOException {
		Path finderHdfsPaths[] = {
				new Path(getPathPrefix() + "/conf/opennlp/en-ner-date.bin"),
				new Path(getPathPrefix() + "/conf/opennlp/en-ner-location.bin"),
				new Path(getPathPrefix() + "/conf/opennlp/en-ner-money.bin"),
				new Path(getPathPrefix()
						+ "/conf/opennlp/en-ner-organization.bin"),
				new Path(getPathPrefix()
						+ "/conf/opennlp/en-ner-percentage.bin"),
				new Path(getPathPrefix() + "/conf/opennlp/en-ner-person.bin"),
				new Path(getPathPrefix() + "/conf/opennlp/en-ner-time.bin") };
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

	@Override
	protected String getModelName() {
		return MODEL;
	}

}
