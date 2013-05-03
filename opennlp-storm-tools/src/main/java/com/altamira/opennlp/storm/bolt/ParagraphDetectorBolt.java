package com.altamira.opennlp.storm.bolt;

import java.io.IOException;
import java.io.StringReader;
import java.util.Map;

import opennlp.tools.util.ParagraphStream;
import opennlp.tools.util.PlainTextByLineStream;
import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseRichBolt;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;
import backtype.storm.tuple.Values;

/**
 * Bolt to split text into paragraphs. Uses OpenNLP's ParagraphStream to simply
 * read "paragraphs" (defined by an empty line preceding it) until null
 * 
 */
public class ParagraphDetectorBolt extends BaseRichBolt {

	private OutputCollector collector;

	public void prepare(Map stormConf, TopologyContext context,
			OutputCollector collector) {
		this.collector = collector;
	}

	public void execute(Tuple input) {
		ParagraphStream samples = new ParagraphStream(
				new PlainTextByLineStream(new StringReader(
						input.getStringByField("text"))));
		try {
			int i = 0;
			String para = samples.read();
			while (para != null) {
				collector.emit(input,
						new Values(input.getStringByField("docId"), i, para));
				i++;
				para = samples.read();
			}
			samples.close();
			collector.ack(input);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void declareOutputFields(OutputFieldsDeclarer declarer) {
		declarer.declare(new Fields("docId", "docParagraphId", "text"));
	}
}
