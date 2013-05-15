package com.altamiracorp.reddawn.ucd.models;

public class Artifact {
  private ArtifactContent content;
  private ArtifactGenericMetadata genericMetadata;
  private ArtifactDynamicMetadata dynamicMetadata;
  private ArtifactKey key;

  private Artifact() {

  }

  public static Builder newBuilder() {
    return new Builder();
  }

  public ArtifactContent getContent() {
    return this.content;
  }

  public ArtifactGenericMetadata getGenericMetadata() {
    return this.genericMetadata;
  }

  public ArtifactDynamicMetadata getDynamicMetadata() {
    return this.dynamicMetadata;
  }

  public ArtifactKey getKey() {
    return this.key;
  }

  public static class Builder {
    private Artifact artifact = new Artifact();

    private Builder() {
    }

    public Builder artifactContent(ArtifactContent artifactContent) {
      this.artifact.content = artifactContent;
      return this;
    }

    public Builder artifactGenericMetadata(ArtifactGenericMetadata artifactGenericMetadata) {
      this.artifact.genericMetadata = artifactGenericMetadata;
      return this;
    }

    public Builder artifactDynamicMetadata(ArtifactDynamicMetadata artifactDynamicMetadata) {
      this.artifact.dynamicMetadata = artifactDynamicMetadata;
      return this;
    }

    public Artifact build() {
      artifact.key = ArtifactKey.newBuilder().docArtifactBytes(this.artifact.content.getDocArtifactBytes()).build();
      return artifact;
    }
  }
}
