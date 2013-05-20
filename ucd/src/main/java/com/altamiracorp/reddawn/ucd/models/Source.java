package com.altamiracorp.reddawn.ucd.models;

import com.google.gson.annotations.Expose;

public class Source {
  public static final String TABLE_NAME = "Source";

  @Expose
  private SourceMetadata metadata;

  @Expose
  private SourceKey key;

  @Expose
  private String uuid;

  private Source() {

  }

  public SourceKey getKey() {
    return this.key;
  }

  public static Builder newBuilder() {
    return new Builder();
  }

  public String getUuid() {
    return uuid;
  }

  public SourceMetadata getMetadata() {
    return metadata;
  }

  public static class Builder {
    private Source source = new Source();

    private Builder() {
    }

    public Builder sourceMetadata(SourceMetadata sourceMetadata) {
      this.source.metadata = sourceMetadata;
      return this;
    }

    public Builder uuid(String uuid) {
      source.uuid = uuid;
      return this;
    }

    public com.altamiracorp.reddawn.ucd.models.Source build() {
      generateKey(source);
      return source;
    }

    private void generateKey(Source source) {
      if (source.getUuid() == null) {
        throw new RuntimeException("UUID cannot be null");
      }

      if (source.getKey() == null) {
        source.key = SourceKey.newBuilder()
                .uuid(source.getUuid())
                .build();
      }
    }
  }
}
