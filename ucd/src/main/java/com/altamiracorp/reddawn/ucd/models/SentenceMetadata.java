package com.altamiracorp.reddawn.ucd.models;

public class SentenceMetadata {
  public static Builder newBuilder() {
    return new Builder();
  }

  public static class Builder {
    private final SentenceMetadata sentenceMetadata = new SentenceMetadata();

    public SentenceMetadata build() {
      return sentenceMetadata;
    }
  }
}
