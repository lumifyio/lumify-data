package com.altamira.opennlp.storm.bolt;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

import opennlp.tools.tokenize.Tokenizer;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;
import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseRichBolt;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;
import backtype.storm.tuple.Values;

/**
 * Bolt to tokenize sentences. Uses a configured model (based on a configured
 * language) with OpenNLP's Maximum-Entropy based Tokenizer
 * 
 */
public class TokenizerBolt extends BaseRichBolt {

	private OutputCollector collector;
	private Tokenizer tokenizer;

	public void prepare(Map stormConf, TopologyContext context,
			OutputCollector collector) {
		this.collector = collector;
		String language = stormConf.get("language").toString();

		// TODO: Determine best (most scalable) way to load "configurable" model
		InputStream modelIn = Thread
				.currentThread()
				.getContextClassLoader()
				.getResourceAsStream(
						stormConf.get(language + ".tokenModel").toString());
		try {
			TokenizerModel model = new TokenizerModel(modelIn);
			this.tokenizer = new TokenizerME(model);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void execute(Tuple input) {
		String[] tokenArray = tokenizer.tokenize(input
				.getStringByField("sentence"));
		ArrayList<String> tokens = new ArrayList<String>(
				Arrays.asList(tokenArray));
		collector.emit(
				input,
				new Values(input.getStringByField("docId"), input
						.getIntegerByField("docParagraphId"), input
						.getIntegerByField("paragraphSentenceId"), tokens));
		collector.ack(input);
	}

	public void declareOutputFields(OutputFieldsDeclarer declarer) {
		declarer.declare(new Fields("docId", "docParagraphId",
				"paragraphSentenceId", "tokens"));
	}

}
