package com.altamiracorp.reddawn.ucd.model.sentence;

import com.altamiracorp.reddawn.ucd.model.artifact.ArtifactKey;
import com.google.gson.annotations.Expose;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class Sentence {
  @Expose
  private ArtifactKey artifactKey;

  @Expose
  private SentenceData sentenceData;

  @Expose
  private SentenceMetadata sentenceMetadata;

  @Expose
  private Map<String, SentenceTerm> sentenceTerm = new HashMap<String, SentenceTerm>();

  public static Builder newBuilder() {
    return new Builder();
  }

  public Collection<SentenceTerm> getSentenceTerm() {
    return sentenceTerm.values();
  }

  public static class Builder {
    private final Sentence sentence = new Sentence();

    public Builder artifactKey(ArtifactKey artifactKey) {
      this.sentence.artifactKey = artifactKey;
      return this;
    }

    public Builder sentenceData(SentenceData sentenceData) {
      this.sentence.sentenceData = sentenceData;
      return this;
    }

    public Builder sentenceMetadata(SentenceMetadata sentenceMetadata) {
      this.sentence.sentenceMetadata = sentenceMetadata;
      return this;
    }

    public Builder sentenceTerm(SentenceTerm sentenceTerm) {
      this.sentence.sentenceTerm.put(sentenceTerm.getFamilyName(), sentenceTerm);
      return this;
    }

    public Sentence build() {
      return sentence;
    }
  }
}
