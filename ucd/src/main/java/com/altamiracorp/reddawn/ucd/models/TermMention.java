package com.altamiracorp.reddawn.ucd.models;

import org.apache.accumulo.core.data.Key;
import org.apache.accumulo.core.data.Mutation;
import org.apache.accumulo.core.data.Value;
import org.apache.commons.codec.binary.Hex;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

public class TermMention {
  public static final String COLUMN_ARTIFACT_KEY = "artifactKey";
  public static final String COLUMN_MENTION = "mention";
  private String rowId;
  private String artifactKey;
  private String mention;

  public void setArtifactKey(String artifactKey) {
    this.rowId = null;
    this.artifactKey = artifactKey;
  }

  public String getArtifactKey() {
    return artifactKey;
  }

  public void setMention(String mention) {
    this.rowId = null;
    this.mention = mention;
  }

  public String getMention() {
    return mention;
  }

  public String getRowId() {
    if (this.rowId == null) {
      try {
        String str = getArtifactKey() + getMention(); // TODO: add other fields here
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(str.getBytes());
        this.rowId = "urn:sha256:" + new String(Hex.encodeHex(hash));
      } catch (NoSuchAlgorithmException ex) {
        throw new RuntimeException(ex); // there is not a lot anyone can do if SHA-256 isn't their so just throw a RuntimeException.
      }
    }
    return this.rowId;
  }

  public void addMutations(Mutation mutation) {
    String columnFamily = getRowId();

    if (getArtifactKey() != null) {
      mutation.put(columnFamily, COLUMN_ARTIFACT_KEY, getArtifactKey());
    }
    if (getMention() != null) {
      mutation.put(columnFamily, COLUMN_MENTION, getMention());
    }
  }

  public static TermMention createFromColumns(String rowId, Collection<Map.Entry<Key, Value>> columns) {
    TermMention termMention = new TermMention();
    for (Map.Entry<Key, Value> column : columns) {
      populateFromColumn(termMention, column.getKey().getColumnQualifier().toString(), column.getValue());
    }
    termMention.rowId = rowId;
    return termMention;
  }

  private static void populateFromColumn(TermMention termMention, String columnQualifier, Value value) {
    if (COLUMN_ARTIFACT_KEY.equals(columnQualifier)) {
      termMention.artifactKey = value.toString();
    } else if (COLUMN_MENTION.equals(columnQualifier)) {
      termMention.mention = value.toString();
    }
  }
}
