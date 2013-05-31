package com.altamiracorp.reddawn.ucd.model.sentence;

import com.altamiracorp.reddawn.ucd.model.terms.TermKey;
import com.google.gson.annotations.Expose;

public class SentenceTerm {
  @Expose
  private TermKey termKey;

  public static Builder newBuilder() {
    return new Builder();
  }

  public String getFamilyName() {
    return termKey.toString();
  }

  public static class Builder {
    private final SentenceTerm sentenceTerm = new SentenceTerm();

    public SentenceTerm build() {
      return sentenceTerm;
    }

    public Builder termKey(TermKey termKey) {
      sentenceTerm.termKey = termKey;
      return this;
    }
  }
}
