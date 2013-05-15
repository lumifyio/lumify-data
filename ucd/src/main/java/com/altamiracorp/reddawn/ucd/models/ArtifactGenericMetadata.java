package com.altamiracorp.reddawn.ucd.models;

public class ArtifactGenericMetadata {
  private String author;
  private String category;
  private String charset;
  private String documentDtg;
  private String documentType;
  private String externalUrl;
  private String extractedTextHdfsPath;
  private String fileExtension;
  private String fileName;
  private long fileSize;
  private long fileTimestamp;
  private String hdfsFilePath;
  private String language;
  private long loadTimestamp;
  private String loadType;
  private String mimeType;
  private String source;
  private String sourceSubType;
  private String sourceType;
  private String subject;

  private ArtifactGenericMetadata() {

  }

  public String getAuthor() {
    return author;
  }

  public String getCategory() {
    return category;
  }

  public String getCharset() {
    return charset;
  }

  public String getDocumentDtg() {
    return documentDtg;
  }

  public String getDocumentType() {
    return documentType;
  }

  public String getExternalUrl() {
    return externalUrl;
  }

  public String getExtractedTextHdfsPath() {
    return extractedTextHdfsPath;
  }

  public String getFileExtension() {
    return fileExtension;
  }

  public String getFileName() {
    return fileName;
  }

  public long getFileSize() {
    return fileSize;
  }

  public long getFileTimestamp() {
    return fileTimestamp;
  }

  public String getHdfsFilePath() {
    return hdfsFilePath;
  }

  public String getLanguage() {
    return language;
  }

  public long getLoadTimestamp() {
    return loadTimestamp;
  }

  public String getLoadType() {
    return loadType;
  }

  public String getMimeType() {
    return mimeType;
  }

  public String getSource() {
    return source;
  }

  public String getSourceSubType() {
    return sourceSubType;
  }

  public String getSourceType() {
    return sourceType;
  }

  public String getSubject() {
    return subject;
  }

  public static Builder newBuilder() {
    return new Builder();
  }

  public static class Builder {
    private ArtifactGenericMetadata artifactGenericMetadata = new ArtifactGenericMetadata();

    private Builder() {

    }

    public ArtifactGenericMetadata build() {
      return this.artifactGenericMetadata;
    }

    public void author(String author) {
      this.artifactGenericMetadata.author = author;
    }

    public void category(String category) {
      this.artifactGenericMetadata.category = category;
    }

    public void charset(String charset) {
      this.artifactGenericMetadata.charset = charset;
    }

    public void documentDtg(String documentDtg) {
      this.artifactGenericMetadata.documentDtg = documentDtg;
    }

    public void documentType(String documentType) {
      this.artifactGenericMetadata.documentType = documentType;
    }

    public void externalUrl(String externalUrl) {
      this.artifactGenericMetadata.externalUrl = externalUrl;
    }

    public void extractedTextHdfsPath(String extractedTextHdfsPath) {
      this.artifactGenericMetadata.extractedTextHdfsPath = extractedTextHdfsPath;
    }

    public void fileExtension(String fileExtension) {
      this.artifactGenericMetadata.fileExtension = fileExtension;
    }

    public void fileName(String fileName) {
      this.artifactGenericMetadata.fileName = fileName;
    }

    public void fileSize(long fileSize) {
      this.artifactGenericMetadata.fileSize = fileSize;
    }

    public void fileTimestamp(long fileTimestamp) {
      this.artifactGenericMetadata.fileTimestamp = fileTimestamp;
    }

    public void hdfsFilePath(String hdfsFilePath) {
      this.artifactGenericMetadata.hdfsFilePath = hdfsFilePath;
    }

    public void language(String language) {
      this.artifactGenericMetadata.language = language;
    }

    public void loadTimestamp(long loadTimestamp) {
      this.artifactGenericMetadata.loadTimestamp = loadTimestamp;
    }

    public void loadType(String loadType) {
      this.artifactGenericMetadata.loadType = loadType;
    }

    public void mimeType(String mimeType) {
      this.artifactGenericMetadata.mimeType = mimeType;
    }

    public void source(String source) {
      this.artifactGenericMetadata.source = source;
    }

    public void sourceSubType(String sourceSubType) {
      this.artifactGenericMetadata.sourceSubType = sourceSubType;
    }

    public void sourceType(String sourceType) {
      this.artifactGenericMetadata.sourceType = sourceType;
    }

    public void subject(String subject) {
      this.artifactGenericMetadata.subject = subject;
    }
  }
}
