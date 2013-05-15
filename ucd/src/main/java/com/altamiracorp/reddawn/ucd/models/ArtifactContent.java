package com.altamiracorp.reddawn.ucd.models;

public class ArtifactContent {
  private String security;
  private String docExtractedText;
  private byte[] docArtifactBytes;

  private ArtifactContent() {

  }

  public static Builder newBuilder() {
    return new Builder();
  }

  public String getSecurity() {
    return this.security;
  }

  public String getDocExtractedText() {
    return docExtractedText;
  }

  public byte[] getDocArtifactBytes() {
    return docArtifactBytes;
  }

  public static class Builder {
    private ArtifactContent artifactContent = new ArtifactContent();

    private Builder() {
    }

    public ArtifactContent build() {
      return artifactContent;
    }

    public void security(String security) {
      artifactContent.security = security;
    }

    public void docExtractedText(String docExtractedText) {
      artifactContent.docExtractedText = docExtractedText;
    }

    public void docArtifactBytes(byte[] docArtifactBytes) {
      artifactContent.docArtifactBytes = docArtifactBytes;
    }
  }
}
