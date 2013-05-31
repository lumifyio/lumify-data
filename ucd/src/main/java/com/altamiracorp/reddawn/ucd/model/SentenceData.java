package com.altamiracorp.reddawn.ucd.model;

public class SentenceData {
  public static Builder newBuilder() {
    return new Builder();
  }

  public static class Builder {
    private final SentenceData sentenceData = new SentenceData();

    public SentenceData build() {
      return sentenceData;
    }
  }
}
