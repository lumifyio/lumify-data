package com.altamiracorp.reddawn.ucd.models;

import com.google.gson.annotations.Expose;
import org.apache.accumulo.core.data.Key;
import org.apache.accumulo.core.data.Mutation;
import org.apache.accumulo.core.data.Value;
import org.json.JSONException;

import java.util.Map;

public class TermMetadata implements Comparable<TermMetadata> {
  private static final String COLUMN_ARTIFACT_KEY = "artifactKey";
  private static final String COLUMN_ARTIFACT_KEY_SIGN = "artifactKeySign";
  private static final String COLUMN_AUTHOR = "author";
  private static final String COLUMN_MENTION = "mention";

  @Expose
  private String artifactKey;

  @Expose
  private String artifactKeySign;

  @Expose
  private String author;

  @Expose
  private TermMention mention;

  public static Builder newBuilder() {
    return new Builder();
  }

  public String getArtifactKey() {
    return artifactKey;
  }

  public String getArtifactKeySign() {
    return artifactKeySign;
  }

  public String getAuthor() {
    return author;
  }

  public TermMention getMention() {
    return mention;
  }

  public void addMutations(Mutation mutation) {
    String columnFamilyName = getColumnFamilyName();
    MutationHelpers.putIfNotNull(mutation, columnFamilyName, COLUMN_ARTIFACT_KEY, getArtifactKey());
    MutationHelpers.putIfNotNull(mutation, columnFamilyName, COLUMN_ARTIFACT_KEY_SIGN, getArtifactKeySign());
    MutationHelpers.putIfNotNull(mutation, columnFamilyName, COLUMN_AUTHOR, getAuthor());
    MutationHelpers.putIfNotNull(mutation, columnFamilyName, COLUMN_MENTION, getMention() == null ? null : getMention().toString());
  }

  @Override
  public int compareTo(TermMetadata termMetadataRight) {
    int start1 = this.getMention() == null ? -1 : this.getMention().getStart();
    int start2 = termMetadataRight == null ? -1 : (termMetadataRight.getMention() == null ? -1 : termMetadataRight.getMention().getStart());
    if (start1 == start2) {
      return 0;
    }
    return start1 > start2 ? 1 : -1;
  }

  public String getColumnFamilyName() {
    String mentionString = null;
    if (getMention() != null) {
      mentionString = getMention().toString();
    }
    return KeyHelpers.createSHA256KeyFromConcatination(new String[]{
        getArtifactKey(),
        getArtifactKeySign(),
        getAuthor(),
        mentionString
    });
  }

  public void populateFromColumn(Map.Entry<Key, Value> column) throws JSONException {
    String columnQualifier = column.getKey().getColumnQualifier().toString();
    if (COLUMN_ARTIFACT_KEY.equals(columnQualifier)) {
      this.artifactKey = column.getValue().toString();
    } else if (COLUMN_ARTIFACT_KEY_SIGN.equals(columnQualifier)) {
      this.artifactKeySign = column.getValue().toString();
    } else if (COLUMN_AUTHOR.equals(columnQualifier)) {
      this.author = column.getValue().toString();
    } else if (COLUMN_MENTION.equals(columnQualifier)) {
      this.mention = new TermMention(column.getValue().toString());
    }
  }

  public static class Builder {
    private final TermMetadata termMetadata;

    private Builder() {
      this.termMetadata = new TermMetadata();
    }

    public Builder artifactKey(String artifactKey) {
      this.termMetadata.artifactKey = artifactKey;
      return this;
    }

    public Builder artifactKeySign(String artifactKeySign) {
      this.termMetadata.artifactKeySign = artifactKeySign;
      return this;
    }

    public Builder author(String author) {
      this.termMetadata.author = author;
      return this;
    }

    public Builder mention(TermMention termMention) {
      this.termMetadata.mention = termMention;
      return this;
    }

    public TermMetadata build() {
      return this.termMetadata;
    }
  }
}
