package com.altamiracorp.reddawn.ucd.models;

import org.apache.accumulo.core.client.RowIterator;
import org.apache.accumulo.core.client.Scanner;
import org.apache.accumulo.core.data.Key;
import org.apache.accumulo.core.data.Mutation;
import org.apache.accumulo.core.data.Value;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.io.FilenameUtils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class Artifact {
  public static final String TABLE_NAME = "Artifact";
  public static final String COLUMN_FAMILY_CONTENT = "Content";
  public static final String COLUMN_CONTENT_DOC_ARTIFACT_BYTES = "doc_artifact_bytes";
  public static final String COLUMN_CONTENT_DOC_EXTRACTED_TEXT = "doc_extracted_text";

  public static final String COLUMN_FAMILY_GENERIC_METADATA = "Generic_Metadata";
  public static final String COLUMN_GENERIC_METADATA_DOCUMENT_DTG = "document_dtg";
  public static final String COLUMN_GENERIC_METADATA_FILE_EXTENSION = "file_extension";
  public static final String COLUMN_GENERIC_METADATA_FILE_NAME = "file_name";
  public static final String COLUMN_GENERIC_METADATA_SUBJECT = "subject";

  private static DateFormat dtgFormat = new SimpleDateFormat("ddHHmm'Z' MMM yy");

  private String rowId;

  // Content
  private byte[] data;
  private byte[] extractedText;

  // Generic Metadata
  private Date documentDateTime;
  private String fileName;
  private String fileExtension;
  private String subject;

  public Artifact() {

  }

  public static List<Artifact> createFromScanner(Scanner scanner) throws ParseException {
    List<Artifact> results = new ArrayList<Artifact>();
    RowIterator rowIterator = new RowIterator(scanner);
    while (rowIterator.hasNext()) {
      Iterator<Map.Entry<Key, Value>> artifactRow = rowIterator.next();
      results.add(createFromRow(artifactRow));
    }
    return results;
  }

  public static Artifact createFromRow(Iterator<Map.Entry<Key, Value>> artifactRow) throws ParseException {
    Artifact result = new Artifact();
    while (artifactRow.hasNext()) {
      Map.Entry<Key, Value> column = artifactRow.next();
      populateArtifactFromScannerEntry(result, column);
    }
    return result;
  }

  private static void populateArtifactFromScannerEntry(Artifact artifact, Map.Entry<Key, Value> scannerEntry) throws ParseException {
    String columnFamily = scannerEntry.getKey().getColumnFamily().toString();
    String columnQualifier = scannerEntry.getKey().getColumnQualifier().toString();
    if (COLUMN_FAMILY_CONTENT.equals(columnFamily)) {
      if (COLUMN_CONTENT_DOC_ARTIFACT_BYTES.equals(columnQualifier)) {
        artifact.data = scannerEntry.getValue().get();
      } else if (COLUMN_CONTENT_DOC_EXTRACTED_TEXT.equals(columnQualifier)) {
        artifact.extractedText = scannerEntry.getValue().get();
      }
    } else if (COLUMN_FAMILY_GENERIC_METADATA.equals(columnFamily)) {
      if (COLUMN_GENERIC_METADATA_FILE_EXTENSION.equals(columnQualifier)) {
        artifact.fileExtension = scannerEntry.getValue().toString();
      } else if (COLUMN_GENERIC_METADATA_FILE_NAME.equals(columnQualifier)) {
        artifact.fileName = scannerEntry.getValue().toString();
      } else if (COLUMN_GENERIC_METADATA_DOCUMENT_DTG.equals(columnQualifier)) {
        artifact.documentDateTime = parseDateTimeGroupString(scannerEntry.getValue().toString());
      } else if (COLUMN_GENERIC_METADATA_SUBJECT.equals(columnQualifier)) {
        artifact.subject = scannerEntry.getValue().toString();
      }
    }
  }

  private static Date parseDateTimeGroupString(String dtgString) throws ParseException {
    return dtgFormat.parse(dtgString);
  }

  private static String dateTimeGroupToString(Date date) {
    return dtgFormat.format(date).toUpperCase();
  }

  public void setFullFileName(String fullFileName) {
    this.fileName = FilenameUtils.getBaseName(fullFileName);
    this.fileExtension = FilenameUtils.getExtension(fullFileName);
  }

  public void setData(byte[] data) {
    this.rowId = null;
    this.data = data;
  }

  public void setData(String data) {
    setData(data.getBytes());
  }

  public Mutation getMutation() {
    Mutation mutation = new Mutation(getRowId());

    // Content
    mutation.put(COLUMN_FAMILY_CONTENT, COLUMN_CONTENT_DOC_ARTIFACT_BYTES, new Value(getData()));
    if (getExtractedText() != null) {
      mutation.put(COLUMN_FAMILY_CONTENT, COLUMN_CONTENT_DOC_EXTRACTED_TEXT, new Value(getExtractedText()));
    }

    // Generic Metadata
    if (getDocumentDateTime() != null) {
      mutation.put(COLUMN_FAMILY_GENERIC_METADATA, COLUMN_GENERIC_METADATA_DOCUMENT_DTG, dateTimeGroupToString(getDocumentDateTime()));
    }
    if (getFileExtension() != null) {
      mutation.put(COLUMN_FAMILY_GENERIC_METADATA, COLUMN_GENERIC_METADATA_FILE_EXTENSION, getFileExtension());
    }
    if (getFileName() != null) {
      mutation.put(COLUMN_FAMILY_GENERIC_METADATA, COLUMN_GENERIC_METADATA_FILE_NAME, getFileName());
    }
    if (getSubject() != null) {
      mutation.put(COLUMN_FAMILY_GENERIC_METADATA, COLUMN_GENERIC_METADATA_SUBJECT, getSubject());
    }
    return mutation;
  }

  public String getRowId() {
    try {
      if (this.rowId != null) {
        return this.rowId;
      }
      MessageDigest digest = MessageDigest.getInstance("SHA-256");
      byte[] hash = digest.digest(data);
      this.rowId = "urn:sha256:" + new String(Hex.encodeHex(hash));
      return this.rowId;
    } catch (NoSuchAlgorithmException ex) {
      throw new RuntimeException(ex); // there is not a lot anyone can do if SHA-256 isn't their so just throw a RuntimeException.
    }
  }

  public String getFileName() {
    return fileName;
  }

  public void setFileName(String fileName) {
    this.fileName = fileName;
  }

  public String getFileExtension() {
    return fileExtension;
  }

  public void setFileExtension(String fileExtension) {
    this.fileExtension = fileExtension;
  }

  public byte[] getData() {
    return this.data;
  }

  public byte[] getExtractedText() {
    return extractedText;
  }

  public void setExtractedText(byte[] extractedText) {
    this.extractedText = extractedText;
  }

  public String getExtractedTextAsString() {
    return new String(this.extractedText);
  }

  public void setExtractedText(String extractedText) {
    this.extractedText = extractedText.getBytes();
  }

  public Date getDocumentDateTime() {
    return documentDateTime;
  }

  public void setDocumentDateTime(Date documentDateTime) {
    this.documentDateTime = documentDateTime;
  }

  public String getSubject() {
    return subject;
  }

  public void setSubject(String subject) {
    this.subject = subject;
  }
}
