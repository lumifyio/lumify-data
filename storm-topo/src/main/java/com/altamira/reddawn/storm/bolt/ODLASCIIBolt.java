package com.altamira.reddawn.storm.bolt;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.Map;

import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseRichBolt;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;
import backtype.storm.tuple.Values;

/**
 * Simple Bolt to pluck out the core contents from ODL ASCII documents. It is
 * not precise by any stretch of the imagination, but strips off most of the
 * metadata that we don't need for our purposes
 * 
 * Oh, and it only works for a handful of the ASCII files I pulled.
 * 
 * TODO: Write a bolt that will handle the JSON format from the ODL
 */
public class ODLASCIIBolt extends BaseRichBolt {

	private OutputCollector collector;

	public void prepare(Map stormConf, TopologyContext context,
			OutputCollector collector) {
		this.collector = collector;

	}

	public void execute(Tuple input) {
		String fullText = input.getStringByField("fullText");
		StringBuilder sb = new StringBuilder();
		BufferedReader br = new BufferedReader(new StringReader(fullText));
		String line = null;
		try {
			line = br.readLine();

			while (line != null) {
				if (line.contains("Document:")) {
					line = br.readLine();
					break;
				}
				line = br.readLine();
			}

			while (line != null) {
				if (!line.contains("Source Description:")) {
					sb.append(line);
					sb.append("\n");
				}
				line = br.readLine();
			}
			br.close();
			collector.emit(input, new Values(input.getStringByField("docId"),
					sb.toString()));
			collector.ack(input);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public void declareOutputFields(OutputFieldsDeclarer declarer) {
		declarer.declare(new Fields("docId", "text"));

	}

}
