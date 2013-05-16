package com.altamiracorp.reddawn.ucd.models;

public class TermMetadata {
  private String artifactKey;
  private String artifactKeySign;
  private String author;
  private TermMention mention;

  public static Builder newBuilder() {
    return new Builder();
  }

  public String getArtifactKey() {
    return artifactKey;
  }

  public String getArtifactKeySign() {
    return artifactKeySign;
  }

  public String getAuthor() {
    return author;
  }

  public TermMention getMention() {
    return mention;
  }

  public static class Builder {
    private final TermMetadata termMetadata;

    private Builder() {
      this.termMetadata = new TermMetadata();
    }

    public Builder artifactKey(String artifactKey) {
      this.termMetadata.artifactKey = artifactKey;
      return this;
    }

    public Builder artifactKeySign(String artifactKeySign) {
      this.termMetadata.artifactKeySign = artifactKeySign;
      return this;
    }

    public Builder author(String author) {
      this.termMetadata.author = author;
      return this;
    }

    public Builder mention(TermMention termMention) {
      this.termMetadata.mention = termMention;
      return this;
    }

    public TermMetadata build() {
      return this.termMetadata;
    }
  }
}
