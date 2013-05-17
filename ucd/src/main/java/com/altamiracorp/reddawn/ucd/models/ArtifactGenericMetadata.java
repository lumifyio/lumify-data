package com.altamiracorp.reddawn.ucd.models;

import com.google.gson.annotations.Expose;
import org.apache.accumulo.core.data.Key;
import org.apache.accumulo.core.data.Mutation;
import org.apache.accumulo.core.data.Value;

import java.util.Map;

public class ArtifactGenericMetadata {
  public static final String COLUMN_FAMILY_NAME = "Generic_Metadata";
  public static final String COLUMN_AUTHOR = "author";
  public static final String COLUMN_CATEGORY = "category";
  public static final String COLUMN_CHARSET = "charset";
  public static final String COLUMN_DOCUMENT_DTG = "document_dtg";
  public static final String COLUMN_DOCUMENT_TYPE = "document_type";
  public static final String COLUMN_EXTERNAL_URL = "external_url";
  public static final String COLUMN_EXTRACTED_TEXT_HDFS_PATH = "extracted_text_hdfs_path";
  public static final String COLUMN_FILE_EXTENSION = "file_extension";
  public static final String COLUMN_FILE_NAME = "file_name";
  public static final String COLUMN_FILE_SIZE = "file_size";
  public static final String COLUMN_FILE_TIMESTAMP = "file_timestamp";
  public static final String COLUMN_HDFS_FILE_PATH = "hdfs_file_path";
  public static final String COLUMN_LANGUAGE = "language";
  public static final String COLUMN_LOAD_TIMESTAMP = "load_timestamp";
  public static final String COLUMN_LOAD_TYPE = "load_type";
  public static final String COLUMN_MIME_TYPE = "mime_type";
  public static final String COLUMN_SOURCE = "source";
  public static final String COLUMN_SOURCE_SUB_TYPE = "source_sub_type";
  private static final String COLUMN_SOURCE_TYPE = "source_type";
  public static final String COLUMN_SUBJECT = "subject";

  @Expose
  private String author;

  @Expose
  private String category;

  @Expose
  private String charset;

  @Expose
  private String documentDtg;

  @Expose
  private String documentType;

  @Expose
  private String externalUrl;

  @Expose
  private String extractedTextHdfsPath;

  @Expose
  private String fileExtension;

  @Expose
  private String fileName;

  @Expose
  private Long fileSize;

  @Expose
  private Long fileTimestamp;

  @Expose
  private String hdfsFilePath;

  @Expose
  private String language;

  @Expose
  private Long loadTimestamp;

  @Expose
  private String loadType;

  @Expose
  private String mimeType;

  @Expose
  private String source;

  @Expose
  private String sourceSubType;

  @Expose
  private String sourceType;

  @Expose
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

  public Long getFileSize() {
    return fileSize;
  }

  public Long getFileTimestamp() {
    return fileTimestamp;
  }

  public String getHdfsFilePath() {
    return hdfsFilePath;
  }

  public String getLanguage() {
    return language;
  }

  public Long getLoadTimestamp() {
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

  void addMutations(Mutation mutation) {
    MutationHelpers.putIfNotNull(mutation, COLUMN_FAMILY_NAME, COLUMN_AUTHOR, getAuthor());
    MutationHelpers.putIfNotNull(mutation, COLUMN_FAMILY_NAME, COLUMN_CATEGORY, getCategory());
    MutationHelpers.putIfNotNull(mutation, COLUMN_FAMILY_NAME, COLUMN_CHARSET, getCharset());
    MutationHelpers.putIfNotNull(mutation, COLUMN_FAMILY_NAME, COLUMN_DOCUMENT_DTG, getDocumentDtg());
    MutationHelpers.putIfNotNull(mutation, COLUMN_FAMILY_NAME, COLUMN_DOCUMENT_TYPE, getDocumentType());
    MutationHelpers.putIfNotNull(mutation, COLUMN_FAMILY_NAME, COLUMN_EXTERNAL_URL, getExternalUrl());
    MutationHelpers.putIfNotNull(mutation, COLUMN_FAMILY_NAME, COLUMN_EXTRACTED_TEXT_HDFS_PATH, getExtractedTextHdfsPath());
    MutationHelpers.putIfNotNull(mutation, COLUMN_FAMILY_NAME, COLUMN_FILE_EXTENSION, getFileExtension());
    MutationHelpers.putIfNotNull(mutation, COLUMN_FAMILY_NAME, COLUMN_FILE_NAME, getFileName());
    MutationHelpers.putIfNotNull(mutation, COLUMN_FAMILY_NAME, COLUMN_FILE_SIZE, getFileSize());
    MutationHelpers.putIfNotNull(mutation, COLUMN_FAMILY_NAME, COLUMN_FILE_TIMESTAMP, getFileTimestamp());
    MutationHelpers.putIfNotNull(mutation, COLUMN_FAMILY_NAME, COLUMN_HDFS_FILE_PATH, getHdfsFilePath());
    MutationHelpers.putIfNotNull(mutation, COLUMN_FAMILY_NAME, COLUMN_LANGUAGE, getLanguage());
    MutationHelpers.putIfNotNull(mutation, COLUMN_FAMILY_NAME, COLUMN_LOAD_TIMESTAMP, getLoadTimestamp());
    MutationHelpers.putIfNotNull(mutation, COLUMN_FAMILY_NAME, COLUMN_LOAD_TYPE, getLoadType());
    MutationHelpers.putIfNotNull(mutation, COLUMN_FAMILY_NAME, COLUMN_MIME_TYPE, getMimeType());
    MutationHelpers.putIfNotNull(mutation, COLUMN_FAMILY_NAME, COLUMN_SOURCE, getSource());
    MutationHelpers.putIfNotNull(mutation, COLUMN_FAMILY_NAME, COLUMN_SOURCE_SUB_TYPE, getSourceSubType());
    MutationHelpers.putIfNotNull(mutation, COLUMN_FAMILY_NAME, COLUMN_SOURCE_TYPE, getSourceType());
    MutationHelpers.putIfNotNull(mutation, COLUMN_FAMILY_NAME, COLUMN_SUBJECT, getSubject());
  }

  public static class Builder {
    private ArtifactGenericMetadata artifactGenericMetadata = new ArtifactGenericMetadata();

    private Builder() {

    }

    public ArtifactGenericMetadata build() {
      return this.artifactGenericMetadata;
    }

    public Builder author(String author) {
      this.artifactGenericMetadata.author = author;
      return this;
    }

    public Builder category(String category) {
      this.artifactGenericMetadata.category = category;
      return this;
    }

    public Builder charset(String charset) {
      this.artifactGenericMetadata.charset = charset;
      return this;
    }

    public Builder documentDtg(String documentDtg) {
      this.artifactGenericMetadata.documentDtg = documentDtg;
      return this;
    }

    public Builder documentType(String documentType) {
      this.artifactGenericMetadata.documentType = documentType;
      return this;
    }

    public Builder externalUrl(String externalUrl) {
      this.artifactGenericMetadata.externalUrl = externalUrl;
      return this;
    }

    public Builder extractedTextHdfsPath(String extractedTextHdfsPath) {
      this.artifactGenericMetadata.extractedTextHdfsPath = extractedTextHdfsPath;
      return this;
    }

    public Builder fileExtension(String fileExtension) {
      this.artifactGenericMetadata.fileExtension = fileExtension;
      return this;
    }

    public Builder fileName(String fileName) {
      this.artifactGenericMetadata.fileName = fileName;
      return this;
    }

    public Builder fileSize(long fileSize) {
      this.artifactGenericMetadata.fileSize = fileSize;
      return this;
    }

    public Builder fileTimestamp(long fileTimestamp) {
      this.artifactGenericMetadata.fileTimestamp = fileTimestamp;
      return this;
    }

    public Builder hdfsFilePath(String hdfsFilePath) {
      this.artifactGenericMetadata.hdfsFilePath = hdfsFilePath;
      return this;
    }

    public Builder language(String language) {
      this.artifactGenericMetadata.language = language;
      return this;
    }

    public Builder loadTimestamp(long loadTimestamp) {
      this.artifactGenericMetadata.loadTimestamp = loadTimestamp;
      return this;
    }

    public Builder loadType(String loadType) {
      this.artifactGenericMetadata.loadType = loadType;
      return this;
    }

    public Builder mimeType(String mimeType) {
      this.artifactGenericMetadata.mimeType = mimeType;
      return this;
    }

    public Builder source(String source) {
      this.artifactGenericMetadata.source = source;
      return this;
    }

    public Builder sourceSubType(String sourceSubType) {
      this.artifactGenericMetadata.sourceSubType = sourceSubType;
      return this;
    }

    public Builder sourceType(String sourceType) {
      this.artifactGenericMetadata.sourceType = sourceType;
      return this;
    }

    public Builder subject(String subject) {
      this.artifactGenericMetadata.subject = subject;
      return this;
    }

    static void populateFromColumn(ArtifactGenericMetadata genericMetadata, Map.Entry<Key, Value> column) {
      String columnQualifier = column.getKey().getColumnQualifier().toString();
      if (COLUMN_AUTHOR.equals(columnQualifier)) {
        genericMetadata.author = column.getValue().toString();
      } else if (COLUMN_CATEGORY.equals(columnQualifier)) {
        genericMetadata.category = column.getValue().toString();
      } else if (COLUMN_CHARSET.equals(columnQualifier)) {
        genericMetadata.charset = column.getValue().toString();
      } else if (COLUMN_DOCUMENT_DTG.equals(columnQualifier)) {
        genericMetadata.documentDtg = column.getValue().toString();
      } else if (COLUMN_DOCUMENT_TYPE.equals(columnQualifier)) {
        genericMetadata.documentType = column.getValue().toString();
      } else if (COLUMN_EXTERNAL_URL.equals(columnQualifier)) {
        genericMetadata.externalUrl = column.getValue().toString();
      } else if (COLUMN_EXTRACTED_TEXT_HDFS_PATH.equals(columnQualifier)) {
        genericMetadata.extractedTextHdfsPath = column.getValue().toString();
      } else if (COLUMN_FILE_EXTENSION.equals(columnQualifier)) {
        genericMetadata.fileExtension = column.getValue().toString();
      } else if (COLUMN_FILE_NAME.equals(columnQualifier)) {
        genericMetadata.fileName = column.getValue().toString();
      } else if (COLUMN_FILE_SIZE.equals(columnQualifier)) {
        genericMetadata.fileSize = ValueHelpers.valueToLong(column.getValue());
      } else if (COLUMN_FILE_TIMESTAMP.equals(columnQualifier)) {
        genericMetadata.fileTimestamp = ValueHelpers.valueToLong(column.getValue());
      } else if (COLUMN_HDFS_FILE_PATH.equals(columnQualifier)) {
        genericMetadata.hdfsFilePath = column.getValue().toString();
      } else if (COLUMN_LANGUAGE.equals(columnQualifier)) {
        genericMetadata.language = column.getValue().toString();
      } else if (COLUMN_LOAD_TIMESTAMP.equals(columnQualifier)) {
        genericMetadata.loadTimestamp = ValueHelpers.valueToLong(column.getValue());
      } else if (COLUMN_LOAD_TYPE.equals(columnQualifier)) {
        genericMetadata.loadType = column.getValue().toString();
      } else if (COLUMN_MIME_TYPE.equals(columnQualifier)) {
        genericMetadata.mimeType = column.getValue().toString();
      } else if (COLUMN_SOURCE.equals(columnQualifier)) {
        genericMetadata.source = column.getValue().toString();
      } else if (COLUMN_SOURCE_SUB_TYPE.equals(columnQualifier)) {
        genericMetadata.sourceSubType = column.getValue().toString();
      } else if (COLUMN_SOURCE_TYPE.equals(columnQualifier)) {
        genericMetadata.sourceType = column.getValue().toString();
      } else if (COLUMN_SUBJECT.equals(columnQualifier)) {
        genericMetadata.subject = column.getValue().toString();
      }
    }
  }
}
