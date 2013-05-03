package com.altamira.reddawn.storm.topology;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import storm.contrib.mongo.SimpleMongoBolt;
import backtype.storm.LocalCluster;
import backtype.storm.generated.AlreadyAliveException;
import backtype.storm.generated.InvalidTopologyException;
import backtype.storm.topology.TopologyBuilder;

import com.altamira.opennlp.storm.bolt.GenericExtractorBolt;
import com.altamira.opennlp.storm.bolt.ParagraphDetectorBolt;
import com.altamira.opennlp.storm.bolt.SentenceDetectorBolt;
import com.altamira.opennlp.storm.bolt.TokenizerBolt;
import com.altamira.reddawn.storm.bolt.ODLASCIIBolt;
import com.altamira.reddawn.storm.bolt.TokenMongoBolt;
import com.altamira.reddawn.storm.spout.SimpleFileListSpout;

/**
 * Topology to cover the NLP bolts. This reads files (based on a wildcard) from
 * a specified directory, and sends them through the NLP "pipeline", saving the
 * results in a mongo db at each step
 * 
 * Currently configured to run in "local mode", but can easily be changed to run
 * on an actual Storm cluster
 */
public class BasicNLPTopology {

	private static final String MONGO_HOST = "localhost";
	private static final int MONGO_PORT = 27017;
	private static final String MONGO_DB = "nlp";

	public static void main(String args[]) throws AlreadyAliveException,
			InvalidTopologyException {
		TopologyBuilder builder = new TopologyBuilder();
		builder.setSpout(
				"files",
				new SimpleFileListSpout(
						new File(
								"/Users/rlanman/Documents/dev/red-dawn/test-data/ascii"),
						"27*"), 2);
		builder.setBolt("textSplitter", new ODLASCIIBolt(), 2).shuffleGrouping(
				"files");

		// take the document text
		builder.setBolt("paragraphDetector", new ParagraphDetectorBolt(), 2)
				.shuffleGrouping("textSplitter");
		builder.setBolt(
				"saveDocument",
				new SimpleMongoBolt(MONGO_HOST, MONGO_PORT, MONGO_DB,
						"documents"), 2).shuffleGrouping("textSplitter");

		// take the paragraph text
		builder.setBolt("sentenceDetector", new SentenceDetectorBolt(), 2)
				.shuffleGrouping("paragraphDetector");
		builder.setBolt(
				"saveParagraph",
				new SimpleMongoBolt(MONGO_HOST, MONGO_PORT, MONGO_DB,
						"paragraphs")).shuffleGrouping("paragraphDetector");

		// take the sentence text
		builder.setBolt("tokenizer", new TokenizerBolt()).shuffleGrouping(
				"sentenceDetector");
		builder.setBolt(
				"saveSentence",
				new SimpleMongoBolt(MONGO_HOST, MONGO_PORT, MONGO_DB,
						"sentences")).shuffleGrouping("sentenceDetector");

		// extract from ALL THE TOKENS! oh, and save the tokens, also
		builder.setBolt("personExtractor", new GenericExtractorBolt("person"))
				.shuffleGrouping("tokenizer");
		builder.setBolt("organizationExtractor",
				new GenericExtractorBolt("organization")).shuffleGrouping(
				"tokenizer");
		builder.setBolt("locationExtractor",
				new GenericExtractorBolt("location")).shuffleGrouping(
				"tokenizer");
		builder.setBolt("saveTokens",
				new TokenMongoBolt(MONGO_HOST, MONGO_PORT, MONGO_DB, "tokens"))
				.shuffleGrouping("tokenizer");

		// save all of our precious entities
		builder.setBolt(
				"saveEntities",
				new SimpleMongoBolt(MONGO_HOST, MONGO_PORT, MONGO_DB,
						"entities")).shuffleGrouping("personExtractor")
				.shuffleGrouping("organizationExtractor")
				.shuffleGrouping("locationExtractor");

		Map conf = buildConfiguration();
		LocalCluster cluster = new LocalCluster();
		cluster.submitTopology("nlptopo", conf, builder.createTopology());
	}

	// TODO: Pull configs out into a props file or something better than
	// hard code. Yuck
	private static Map buildConfiguration() {
		Map conf = new HashMap();
		conf.put("language", "en");
		conf.put("en.sentenceModel", "models/en-sent.bin");
		conf.put("en.tokenModel", "models/en-token.bin");
		conf.put("en.person.finderModel", "models/en-ner-person.bin");
		conf.put("en.organization.finderModel",
				"models/en-ner-organization.bin");
		conf.put("en.location.finderModel", "models/en-ner-location.bin");
		return conf;
	}
}
