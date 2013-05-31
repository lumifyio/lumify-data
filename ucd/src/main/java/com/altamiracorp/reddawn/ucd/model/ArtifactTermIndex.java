package com.altamiracorp.reddawn.ucd.model;

import com.google.gson.annotations.Expose;
import org.apache.accumulo.core.client.RowIterator;
import org.apache.accumulo.core.client.Scanner;
import org.apache.accumulo.core.data.Key;
import org.apache.accumulo.core.data.Mutation;
import org.apache.accumulo.core.data.Value;

import java.util.*;

public class ArtifactTermIndex {
  public static final String TABLE_NAME = "ArtifactTermIndex";
  @Expose
  private ArtifactKey key;

  @Expose
  private Map<TermKey, List<String>> termMentions = new HashMap<TermKey, List<String>>();

  public static Builder newBuilder() {
    return new Builder();
  }

  public ArtifactKey getKey() {
    return this.key;
  }

  public Map<TermKey, List<String>> getTermMentions() {
    return this.termMentions;
  }

  public Mutation getMutation() {
    Mutation mutation = new Mutation(getKey().toString());
    for (TermKey termRowId : this.termMentions.keySet()) {
      for (String termTableColumnFamilyName : this.termMentions.get(termRowId)) {
        mutation.put(termRowId.toString(), termTableColumnFamilyName, "yes"); // TODO what should the value be?
      }
    }
    return mutation;
  }

  private void findAddTermMention(TermKey termRowId, String termTableColumnFamilyName) {
    List<String> mentions;
    if (this.termMentions.containsKey(termRowId)) {
      mentions = this.termMentions.get(termRowId);
    } else {
      mentions = new ArrayList<String>();
      this.termMentions.put(termRowId, mentions);
    }
    mentions.add(termTableColumnFamilyName);
  }

  public static class Builder {
    private ArtifactTermIndex artifactTermIndex;

    private Builder() {
      this.artifactTermIndex = new ArtifactTermIndex();
    }

    public Builder artifactKey(ArtifactKey artifactKey) {
      this.artifactTermIndex.key = artifactKey;
      return this;
    }

    public Builder termMention(TermKey termRowId, String termTableColumnFamilyName) {
      this.artifactTermIndex.findAddTermMention(termRowId, termTableColumnFamilyName);
      return this;
    }

    public ArtifactTermIndex build() {
      return this.artifactTermIndex;
    }

    public List<ArtifactTermIndex> buildFromScanner(Scanner scanner) {
      List<ArtifactTermIndex> results = new ArrayList<ArtifactTermIndex>();
      RowIterator rowIterator = new RowIterator(scanner);
      while (rowIterator.hasNext()) {
        Iterator<Map.Entry<Key, Value>> columns = rowIterator.next();
        results.add(buildFromRow(columns));
      }
      return results;
    }

    public ArtifactTermIndex buildFromRow(Iterator<Map.Entry<Key, Value>> columns) {
      ArtifactTermIndex result = new ArtifactTermIndex();
      while (columns.hasNext()) {
        Map.Entry<Key, Value> column = columns.next();
        if (result.key == null) {
          result.key = new ArtifactKey(column.getKey().getRow().toString());
        }
        populateFromColumn(result, column);
      }
      return result;
    }

    private void populateFromColumn(ArtifactTermIndex artifactTermIndex, Map.Entry<Key, Value> column) {
      String columnFamily = column.getKey().getColumnFamily().toString();
      String columnQualifier = column.getKey().getColumnQualifier().toString();
      artifactTermIndex.findAddTermMention(new TermKey(columnFamily), columnQualifier);
    }
  }
}
