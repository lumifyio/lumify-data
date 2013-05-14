package com.altamira.opennlp.storm.bolt;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

import opennlp.tools.namefind.NameFinderME;
import opennlp.tools.namefind.TokenNameFinder;
import opennlp.tools.namefind.TokenNameFinderModel;
import opennlp.tools.util.Span;
import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseRichBolt;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;
import backtype.storm.tuple.Values;

/**
 * Generic bolt that uses the Maximum-Entropy NameFinder from OpenNLP with a
 * specified model to "extract" entities from pre-tokenized sentences
 * 
 */
public class GenericExtractorBolt extends BaseRichBolt {

	private OutputCollector collector;
	private String entityType;
	private TokenNameFinder extractor;

	public GenericExtractorBolt(String entityType) {
		this.entityType = entityType;
	}

	public void prepare(Map stormConf, TopologyContext context,
			OutputCollector collector) {
		this.collector = collector;
		String language = (String) stormConf.get("language");

		// TODO: Determine best (most scalable) way to load "configurable" model
		String modelPath = stormConf.get(
				language + "." + entityType + ".finderModel").toString();
		InputStream modelIn = Thread.currentThread().getContextClassLoader()
				.getResourceAsStream(modelPath);
		TokenNameFinderModel model = null;
		try {
			model = new TokenNameFinderModel(modelIn);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		extractor = new NameFinderME(model);
	}

	public void execute(Tuple input) {
		// TODO: find a better way to pass the tokens around. Something weird
		// with generics happening here. I was getting a ClassCastException with
		// toArray(), so I had to brute force it
		List<String> tokens = (List<String>) input.getValueByField("tokens");
		String[] tokenArr = new String[tokens.size()];
		for (int i = 0; i < tokenArr.length; i++) {
			tokenArr[i] = tokens.get(i).toString();
		}

		// "extract" the entities
		Span[] entitySpans = extractor.find(tokenArr);
		for (int i = 0; i < entitySpans.length; i++) {
			// build the string value of the entity
			// TODO: look under the hood of OpenNLP and see if their
			// spansToStrings method does any better than this
			Span entitySpan = entitySpans[i];
			StringBuilder entity = new StringBuilder();
			for (int j = entitySpan.getStart(); j < entitySpan.getEnd(); j++) {
				entity.append(tokenArr[j]);
				if (j + 1 < entitySpan.getEnd()) {
					entity.append(" ");
				}
			}
			collector.emit(
					input,
					new Values(input.getStringByField("docId"), input
							.getIntegerByField("docParagraphId"), input
							.getIntegerByField("paragraphSentenceId"), i,
							entityType, entitySpan.getStart(), entitySpan
									.getEnd(), entity.toString()));
			collector.ack(input);
		}
	}

	public void declareOutputFields(OutputFieldsDeclarer declarer) {
		declarer.declare(new Fields("docId", "docParagraphId",
				"paragraphSentenceId", "sentenceEntityId", "entityType",
				"start", "end", "entity"));

	}

}
