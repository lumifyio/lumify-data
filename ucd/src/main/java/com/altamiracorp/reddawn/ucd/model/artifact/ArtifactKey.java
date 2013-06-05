package com.altamiracorp.reddawn.ucd.model.artifact;

import com.altamiracorp.reddawn.ucd.model.KeyHelpers;
import com.altamiracorp.reddawn.ucd.model.base.BaseDTO;
import com.google.common.collect.ComparisonChain;
import com.google.common.collect.Ordering;
import com.google.gson.annotations.Expose;


public class ArtifactKey implements BaseDTO<ArtifactKey> {
  @Expose
  private String artifactKey;

  private ArtifactKey() {
  }

  public ArtifactKey(String key) {
    this.artifactKey = key;
  }

  public static Builder newBuilder() {
    return new Builder();
  }

  @Override
  public int compareTo(ArtifactKey rhs) {
    return ComparisonChain
            .start()
            .compare(artifactKey, rhs.artifactKey,
                    Ordering.natural().nullsFirst()).result();
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result
            + ((artifactKey == null) ? 0 : artifactKey.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    ArtifactKey other = (ArtifactKey) obj;
    if (artifactKey == null) {
      if (other.artifactKey != null)
        return false;
    } else if (!artifactKey.equals(other.artifactKey))
      return false;
    return true;
  }

  @Override
  public String toString() {
    return this.artifactKey;
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
      artifactKey.artifactKey = KeyHelpers.createSHA256Key(docArtifactBytes);
      return artifactKey;
    }
  }
}
