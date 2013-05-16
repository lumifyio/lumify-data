package com.altamiracorp.reddawn.ucd.models;

import com.google.gson.annotations.Expose;

public class ArtifactKey {
  @Expose
  private String key;

  private ArtifactKey() {
  }

  public ArtifactKey(String key) {
    this.key = key;
  }

  @Override
  public String toString() {
    return this.key;
  }

  public static Builder newBuilder() {
    return new Builder();
  }

  public static class Builder {
    private byte[] docArtifactBytes;

    public Builder docArtifactBytes(byte[] docArtifactBytes) {
      this.docArtifactBytes = docArtifactBytes;
      return this;
    }

    public ArtifactKey build() {
      ArtifactKey artifactKey = new ArtifactKey();
      if (docArtifactBytes == null) {
        throw new RuntimeException("docArtifactBytes cannot be null");
      }
      artifactKey.key = KeyHelpers.createSHA256Key(docArtifactBytes);
      return artifactKey;
    }
  }
}
