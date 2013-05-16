package com.altamiracorp.reddawn.ucd.models;

public class Term {
  private TermKey key;
  private TermMetadata metadata;

  public static Builder newBuilder() {
    return new Builder();
  }

  public TermKey getKey() {
    return key;
  }

  public TermMetadata getMetadata() {
    return metadata;
  }

  public static class Builder {
    private final Term term;

    private Builder() {
      this.term = new Term();
    }

    public Builder key(TermKey termKey) {
      this.term.key = termKey;
      return this;
    }

    public Builder metadata(TermMetadata termMetadata) {
      this.term.metadata = termMetadata;
      return this;
    }

    public Term build() {
      return this.term;
    }
  }
}
