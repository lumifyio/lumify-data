package com.altamiracorp.redDawn.ucd.models;

import org.apache.accumulo.core.client.Scanner;
import org.apache.accumulo.core.data.Key;
import org.apache.accumulo.core.data.Mutation;
import org.apache.accumulo.core.data.Value;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.io.FilenameUtils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class Artifact {
  public static final String TABLE_NAME = "Artifact";
  public static final String COLUMN_FAMILY_CONTENT = "Content";
  public static final String COLUMN_CONTENT_DOC_ARTIFACT_BYTES = "doc_artifact_bytes";
  public static final String COLUMN_FAMILY_GENERIC_METADATA = "Generic_Metadata";
  public static final String COLUMN_GENERIC_METADATA_FILE_EXTENSION = "file_extension";
  public static final String COLUMN_GENERIC_METADATA_FILE_NAME = "file_name";

  private byte[] data;
  private String fileName;
  private String fileExtension;
  private String rowId;

  public Artifact() {

  }

  public static List<Artifact> createFromScanner(Scanner scanner) {
    Iterator<Map.Entry<Key, Value>> it = scanner.iterator();
    if (!it.hasNext()) {
      return null;
    }
    List<Artifact> result = new ArrayList<Artifact>();
    Artifact currentArtifact = new Artifact();
    Map.Entry<Key, Value> scannerEntry = it.next();
    currentArtifact.rowId = scannerEntry.getKey().getRow().toString();
    populateArtifactFromScannerEntry(currentArtifact, scannerEntry);
    while (it.hasNext()) {
      scannerEntry = it.next();
      if (!scannerEntry.getKey().getRow().toString().equals(currentArtifact.rowId)) {
        result.add(currentArtifact);
        currentArtifact = new Artifact();
      }
      populateArtifactFromScannerEntry(currentArtifact, scannerEntry);
    }
    result.add(currentArtifact);
    return result;
  }

  private static void populateArtifactFromScannerEntry(Artifact result, Map.Entry<Key, Value> scannerEntry) {
    String columnFamily = scannerEntry.getKey().getColumnFamily().toString();
    String columnQualifier = scannerEntry.getKey().getColumnQualifier().toString();
    if (COLUMN_FAMILY_CONTENT.equals(columnFamily)) {
      if (COLUMN_CONTENT_DOC_ARTIFACT_BYTES.equals(columnQualifier)) {
        result.data = scannerEntry.getValue().get();
      }
    } else if (COLUMN_FAMILY_GENERIC_METADATA.equals(columnFamily)) {
      if (COLUMN_GENERIC_METADATA_FILE_EXTENSION.equals(columnQualifier)) {
        result.fileExtension = scannerEntry.getValue().toString();
      } else if (COLUMN_GENERIC_METADATA_FILE_NAME.equals(columnQualifier)) {
        result.fileName = scannerEntry.getValue().toString();
      }
    }
  }

  public void setFullFileName(String fullFileName) {
    this.fileName = FilenameUtils.getBaseName(fullFileName);
    this.fileExtension = FilenameUtils.getExtension(fullFileName);
  }

  public void setData(String data) {
    this.rowId = null;
    this.data = data.getBytes();
  }

  public Mutation getMutation() {
    Mutation mutation = new Mutation(getRowId());
    mutation.put(COLUMN_FAMILY_CONTENT, COLUMN_CONTENT_DOC_ARTIFACT_BYTES, new Value(this.data));
    if (this.fileExtension != null) {
      mutation.put(COLUMN_FAMILY_GENERIC_METADATA, COLUMN_GENERIC_METADATA_FILE_EXTENSION, this.fileExtension);
    }
    if (this.fileName != null) {
      mutation.put(COLUMN_FAMILY_GENERIC_METADATA, COLUMN_GENERIC_METADATA_FILE_NAME, this.fileName);
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
}
