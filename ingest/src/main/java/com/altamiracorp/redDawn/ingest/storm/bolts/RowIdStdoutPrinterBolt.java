package com.altamiracorp.redDawn.ingest.storm.bolts;

import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseRichBolt;
import backtype.storm.tuple.Tuple;

import java.util.Map;

public class RowIdStdoutPrinterBolt extends BaseRichBolt {
  private OutputCollector outputCollector;

  public RowIdStdoutPrinterBolt() {
  }

  @Override
  public void prepare(Map map, TopologyContext topologyContext, OutputCollector outputCollector) {
    this.outputCollector = outputCollector;
  }

  @Override
  public void execute(Tuple input) {
    String rowId = input.getStringByField(FileDataToUcdArtifactBolt.FIELD_ROW_ID);
    System.out.println(rowId);
    this.outputCollector.ack(input);
  }

  @Override
  public void declareOutputFields(OutputFieldsDeclarer outputFieldsDeclarer) {
  }
}
