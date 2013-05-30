package com.altamiracorp.reddawn.entityExtraction;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import opennlp.tools.dictionary.Dictionary;

import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;

/**
 * Keeping this code separate so that we can maybe access the dictionary files
 * from the "update" workflow. A little more reusable than keeping it in the
 * actual Dictionary entity extractor, but could probably use some work.
 * 
 * @author rlanman
 * 
 */
public class OpenNlpDictionaryRegistry {

	// TODO: We should probably make this configurable/not hard-coded
	private Map<String, String> dictionaryPaths = new HashMap<String, String>();

	private Map<String, Dictionary> dictionaries = new HashMap<String, Dictionary>();

	public void loadRegistry(String pathPrefix, FileSystem fs)
			throws IOException {
		loadDictionaries(pathPrefix, fs);
	}

	public void loadDictionaries(String pathPrefix, FileSystem fs)
			throws IOException {
		for (Entry<String, String> entry : dictionaryPaths.entrySet()) {
			Path hdfsPath = new Path(pathPrefix + entry.getValue());
			InputStream dictionaryInputStream = fs.open(hdfsPath);
			try {
				dictionaries.put(entry.getKey(), new Dictionary(
						dictionaryInputStream));
			} finally {
				dictionaryInputStream.close();
			}
		}
	}

	public Dictionary getSingleDictionary(String key) {
		return dictionaries.get(key);
	}

	public Collection<Dictionary> getAllDictionaries() {
		return dictionaries.values();
	}

	private void initPaths() {
		dictionaryPaths.put("ner-location",
				"/conf/opennlp/en-ner-location.dict");
		dictionaryPaths.put("ner-organization",
				"/conf/opennlp/en-ner-organization.dict");
		dictionaryPaths.put("ner-person", "/conf/opennlp/en-ner-person.dict");
	}

}
