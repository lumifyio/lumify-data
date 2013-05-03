package com.altamira.opennlp.storm.bolt;

import java.io.InputStream;
import java.util.Map;

import opennlp.tools.sentdetect.SentenceDetector;
import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;
import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseRichBolt;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;
import backtype.storm.tuple.Values;

/**
 * Bolt to split paragraphs into sentences. Uses a configured model (based on a
 * configured language) with OpenNLP's Maximum-Entropy based SentenceDetector
 * 
 */
public class SentenceDetectorBolt extends BaseRichBolt {

	private OutputCollector collector;
	private SentenceDetector detector;

	public void prepare(Map stormConf, TopologyContext context,
			OutputCollector collector) {
		this.collector = collector;
		String language = stormConf.get("language").toString();

		// TODO: Determine best (most scalable) way to load "configurable" model
		InputStream modelIn = Thread
				.currentThread()
				.getContextClassLoader()
				.getResourceAsStream(
						stormConf.get(language + ".sentenceModel").toString());
		try {
			SentenceModel model = new SentenceModel(modelIn);
			this.detector = new SentenceDetectorME(model);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void execute(Tuple input) {
		String docText = input.getStringByField("text");
		String[] sentences = detector.sentDetect(docText);
		for (int i = 0; i < sentences.length; i++) {
			collector
					.emit(input, new Values(input.getStringByField("docId"),
							input.getIntegerByField("docParagraphId"), i,
							sentences[i]));
		}
		collector.ack(input);
	}

	public void declareOutputFields(OutputFieldsDeclarer declarer) {
		declarer.declare(new Fields("docId", "docParagraphId",
				"paragraphSentenceId", "sentence"));

	}
}
