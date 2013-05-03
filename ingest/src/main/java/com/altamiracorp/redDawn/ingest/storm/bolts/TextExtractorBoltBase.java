package com.altamiracorp.redDawn.ingest.storm.bolts;

import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseRichBolt;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;
import backtype.storm.tuple.Values;
import com.altamiracorp.redDawn.ingest.storm.spouts.FileReaderSpout;

import java.util.Map;

public abstract class TextExtractorBoltBase extends BaseRichBolt {
  public static final String FIELD_EXTRACTED_TEXT = "extractedText";

  private OutputCollector outputCollector;

  @Override
  public void prepare(Map map, TopologyContext topologyContext, OutputCollector outputCollector) {
    this.outputCollector = outputCollector;
  }

  @Override
  public void execute(Tuple input) {
    String fileName = input.getStringByField(FileReaderSpout.FIELD_FILE_NAME);
    long timestamp = input.getLongByField(FileReaderSpout.FIELD_TIMESTAMP);
    byte[] data = input.getBinaryByField(FileReaderSpout.FIELD_DATA);
    String str = extractText(fileName, data);
    this.outputCollector.emit(new Values(fileName, timestamp, data, str));
    this.outputCollector.ack(input);
  }

  protected abstract String extractText(String fileName, byte[] data);

  @Override
  public void declareOutputFields(OutputFieldsDeclarer outputFieldsDeclarer) {
    outputFieldsDeclarer.declare(new Fields(
        FileReaderSpout.FIELD_FILE_NAME,
        FileReaderSpout.FIELD_TIMESTAMP,
        FileReaderSpout.FIELD_DATA,
        FIELD_EXTRACTED_TEXT));
  }
}
