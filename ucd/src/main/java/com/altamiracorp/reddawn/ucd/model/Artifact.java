package com.altamiracorp.reddawn.ucd.model;

import com.altamiracorp.reddawn.ucd.model.artifact.ArtifactKey;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;
import org.apache.accumulo.core.client.RowIterator;
import org.apache.accumulo.core.client.Scanner;
import org.apache.accumulo.core.data.Key;
import org.apache.accumulo.core.data.Mutation;
import org.apache.accumulo.core.data.Value;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class Artifact {
  public static final String TABLE_NAME = "Artifact";

  @Expose
  private ArtifactContent content;

  @Expose
  private ArtifactGenericMetadata genericMetadata;

  @Expose
  private ArtifactDynamicMetadata dynamicMetadata;

  @Expose
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

  public Mutation getMutation() {
    Mutation mutation = new Mutation(getKey().toString());
    if (getContent() != null) {
      getContent().addMutations(mutation);
    }
    if (getGenericMetadata() != null) {
      getGenericMetadata().addMutations(mutation);
    }
    if (getDynamicMetadata() != null) {
      getDynamicMetadata().addMutations(mutation);
    }
    return mutation;
  }

  public String toJson() {
    Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
    return gson.toJson(this);
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
      generateKey(artifact);
      return artifact;
    }

    private static void generateKey(Artifact artifact) {
      if (artifact.getContent() == null && artifact.getKey() == null) {
        throw new RuntimeException("Content and Key cannot be null");
      }
      if (artifact.getKey() == null && artifact.getContent().getDocArtifactBytes() == null) {
        throw new RuntimeException("Key and Content.DocArtifactBytes cannot be null");
      }
      if (artifact.getKey() == null) {
        artifact.key = ArtifactKey.newBuilder()
            .docArtifactBytes(artifact.getContent().getDocArtifactBytes())
            .build();
      }
    }

    public List<Artifact> buildFromScanner(Scanner scanner) {
      List<Artifact> results = new ArrayList<Artifact>();
      RowIterator rowIterator = new RowIterator(scanner);
      while (rowIterator.hasNext()) {
        Iterator<Map.Entry<Key, Value>> columns = rowIterator.next();
        results.add(buildFromRow(columns));
      }
      return results;
    }

    public Artifact buildFromRow(Iterator<Map.Entry<Key, Value>> columns) {
      Artifact result = new Artifact();
      result.content = ArtifactContent.newBuilder().build();
      result.dynamicMetadata = ArtifactDynamicMetadata.newBuilder().build();
      result.genericMetadata = ArtifactGenericMetadata.newBuilder().build();
      while (columns.hasNext()) {
        Map.Entry<Key, Value> column = columns.next();
        if (result.key == null) {
          result.key = new ArtifactKey(column.getKey().getRow().toString());
        }
        populateFromColumn(result, column);
      }
      return result;
    }

    private void populateFromColumn(Artifact artifact, Map.Entry<Key, Value> column) {
      String columnFamily = column.getKey().getColumnFamily().toString();
      if (ArtifactContent.COLUMN_FAMILY_NAME.equals(columnFamily)) {
        ArtifactContent.Builder.populateFromColumn(artifact.getContent(), column);
      } else if (ArtifactGenericMetadata.COLUMN_FAMILY_NAME.equals(columnFamily)) {
        ArtifactGenericMetadata.Builder.populateFromColumn(artifact.getGenericMetadata(), column);
      } else if (ArtifactDynamicMetadata.COLUMN_FAMILY_NAME.equals(columnFamily)) {
        ArtifactDynamicMetadata.Builder.populateFromColumn(artifact.getDynamicMetadata(), column);
      }
    }
  }
}
