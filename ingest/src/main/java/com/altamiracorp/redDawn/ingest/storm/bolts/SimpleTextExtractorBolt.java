package com.altamiracorp.redDawn.ingest.storm.bolts;

import backtype.storm.tuple.Tuple;

public class SimpleTextExtractorBolt extends TextExtractorBoltBase {
  @Override
  protected String extractText(String fileName, byte[] data) {
    return new String(data);
  }
}
