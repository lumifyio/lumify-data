package com.altamiracorp.reddawn.entityExtraction;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import opennlp.tools.namefind.TokenNameFinderModel;
import opennlp.tools.tokenize.TokenizerModel;
import opennlp.tools.util.model.BaseModel;

import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.mapreduce.Mapper.Context;

public class OpenNlpModelRegistry {
	public static final String PATH_PREFIX = "nlpConfPathPrefix";
	public static final String DEFAULT_PATH_PREFIX = "hdfs://";

	private Map<String, List<BaseModel>> modelMap;
	private String pathPrefix;

	public void loadRegistry(Context context) throws IOException {
		FileSystem fs = FileSystem.get(context.getConfiguration());
		this.pathPrefix = context.getConfiguration().get(PATH_PREFIX,
				DEFAULT_PATH_PREFIX);
		
		loadFinders(fs);
		loadTokenizer(fs);
	}

	public BaseModel getSingleModel(String key) {
		List<BaseModel> models = modelMap.get(key);
		return models == null ? null : models.get(0);
	}

	public List<BaseModel> getModels(String key) {
		return modelMap.get(key);
	}

	private void loadFinders(FileSystem fs) throws IOException {
		Path finderHdfsPaths[] = {
				new Path(pathPrefix + "/conf/opennlp/en-ner-date.bin"),
				new Path(pathPrefix + "/conf/opennlp/en-ner-location.bin"),
				new Path(pathPrefix + "/conf/opennlp/en-ner-money.bin"),
				new Path(pathPrefix + "/conf/opennlp/en-ner-organization.bin"),
				new Path(pathPrefix + "/conf/opennlp/en-ner-percentage.bin"),
				new Path(pathPrefix + "/conf/opennlp/en-ner-person.bin"),
				new Path(pathPrefix + "/conf/opennlp/en-ner-time.bin") };

		ArrayList<BaseModel> finderModels = new ArrayList<BaseModel>();
		for (Path finderHdfsPath : finderHdfsPaths) {
			InputStream finderModelInputStream = fs.open(finderHdfsPath);
			try {
				finderModels.add(new TokenNameFinderModel(
						finderModelInputStream));
			} finally {
				finderModelInputStream.close();
			}
		}

		modelMap.put("finders", finderModels);
	}

	private void loadTokenizer(FileSystem fs) throws IOException {
		Path tokenizerHdfsPath = new Path(pathPrefix
				+ "/conf/opennlp/en-token.bin");

		ArrayList<BaseModel> tokenizerModels = new ArrayList<BaseModel>();
		InputStream tokenizerModelInputStream = fs.open(tokenizerHdfsPath);
		try {
			tokenizerModels.add(new TokenizerModel(tokenizerModelInputStream));
		} finally {
			tokenizerModelInputStream.close();
		}

		modelMap.put("tokenizer", tokenizerModels);
	}

}
