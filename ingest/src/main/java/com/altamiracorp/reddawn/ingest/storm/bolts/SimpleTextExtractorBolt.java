package com.altamiracorp.reddawn.ingest.storm.bolts;

public class SimpleTextExtractorBolt extends TextExtractorBoltBase {
  @Override
  protected String extractText(String fileName, byte[] data) {
    return new String(data);
  }
}
