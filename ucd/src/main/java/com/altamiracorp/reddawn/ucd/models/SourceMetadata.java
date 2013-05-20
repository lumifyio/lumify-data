package com.altamiracorp.reddawn.ucd.models;

import com.google.gson.annotations.Expose;
import org.apache.accumulo.core.data.Key;
import org.apache.accumulo.core.data.Mutation;
import org.apache.accumulo.core.data.Value;

import java.util.Map;

public class SourceMetadata {
  public static final String COLUMN_FAMILY_NAME = "Source";
  private static final String COLUMN_ACRONYM = "acronym";

  private SourceMetadata() {

  }

  public static Builder newBuilder() {
    return new Builder();
  }

  public static class Builder {
    private SourceMetadata artifactGenericMetadata = new SourceMetadata();

    private Builder() {
    }

    public SourceMetadata build() {
      return this.artifactGenericMetadata;
    }

    static void populateFromColumn(SourceMetadata genericMetadata, Map.Entry<Key, Value> column) {
    }

    public void acronym(String dod) {
    }
  }
}
