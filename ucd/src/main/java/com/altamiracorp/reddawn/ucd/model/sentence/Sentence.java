package com.altamiracorp.reddawn.ucd.model.sentence;

import com.google.gson.annotations.Expose;

import java.util.List;

public class Sentence {
  @Expose
  private SentenceKey sentenceKey;

  @Expose
  private SentenceData sentenceData;

  @Expose
  private SentenceMetadata sentenceMetadata;

  @Expose
  private List<SentenceTermId> termIds;

  Sentence(Builder builder) {
    this.sentenceKey = builder.sentenceKey;
    this.sentenceData = builder.sentenceData;
    this.sentenceMetadata = builder.sentenceMetadata;
    this.termIds = builder.termIds;
  }

  public static Builder newBuilder() {
    return new Builder();
  }

  public List<SentenceTermId> getTermIds() {
    return termIds;
  }

  public SentenceKey getSentenceKey() {
    return sentenceKey;
  }

  public SentenceData getSentenceData() {
    return sentenceData;
  }

  public static class Builder {
    private SentenceKey sentenceKey;
    private SentenceData sentenceData;
    private SentenceMetadata sentenceMetadata;
    private List<SentenceTermId> termIds;

    public Builder sentenceKey(SentenceKey sentenceKey) {
      this.sentenceKey = sentenceKey;
      return this;
    }

    public Builder sentenceData(SentenceData sentenceData) {
      this.sentenceData = sentenceData;
      return this;
    }

    public Builder sentenceMetadata(SentenceMetadata sentenceMetadata) {
      this.sentenceMetadata = sentenceMetadata;
      return this;
    }

    public Builder termIds(List<SentenceTermId> termIds) {
      this.termIds = termIds;
      return this;
    }

    public Sentence build() {
      return new Sentence(this);
    }
  }
}
