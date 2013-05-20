package com.altamiracorp.reddawn.ucd.models;

public class SourceMetadata {
  public static final String COLUMN_FAMILY_NAME = "Source";
  private static final String COLUMN_ACRONYM = "acronym";
  private String acronym;

  private SourceMetadata() {

  }

  public static Builder newBuilder() {
    return new Builder();
  }

  public String getAcronym() {
    return acronym;
  }

  public static class Builder {
    private SourceMetadata metadata = new SourceMetadata();

    private Builder() {
    }

    public SourceMetadata build() {
      return this.metadata;
    }

    public void acronym(String acronym) {
      metadata.acronym = acronym;
    }
  }
}
