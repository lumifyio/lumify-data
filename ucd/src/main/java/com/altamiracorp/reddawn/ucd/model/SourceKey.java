package com.altamiracorp.reddawn.ucd.model;

import com.google.gson.annotations.Expose;

public class SourceKey {
  @Expose
  private String key;

  public static Builder newBuilder() {
    return new Builder();
  }

  @Override
  public String toString() {
    return this.key;
  }

  public static class Builder {
    private String uuid;

    public SourceKey build() {
      SourceKey SourceKey = new SourceKey();
      if (uuid == null) {
        throw new RuntimeException("uuid cannot be null");
      }
      SourceKey.key = uuid;
      return SourceKey;
    }

    public Builder uuid(String uuid) {
      this.uuid = uuid;
      return this;
    }
  }
}
