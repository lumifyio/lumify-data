package com.altamiracorp.reddawn.ucd.models;

import org.apache.accumulo.core.data.Key;
import org.apache.accumulo.core.data.Mutation;
import org.apache.accumulo.core.data.Value;

import java.util.Map;

public class ArtifactDynamicMetadata {
  public static final String COLUMN_FAMILY_NAME = "Dynamic_Metadata";
  private static final String COLUMN_ARTIFACT_SERIAL_NUM = "artifact_serial_num";
  private static final String COLUMN_DOC_SOURCE_HASH = "doc_source_hash";
  private static final String COLUMN_EDH_GUID = "edh_guid";
  private static final String COLUMN_GEO_LOCATION = "geo_location";
  private static final String COLUMN_PROVENANCE_ID = "provenance_id";
  private static final String COLUMN_SOURCE_HASH_ALGORITHM = "source_hash_algorithm";
  private static final String COLUMN_SOURCE_LABEL = "source_label";
  private String artifactSerialNum;
  private String docSourceHash;
  private String edhGuid;
  private String geoLocation;
  private String provenanceId;
  private String sourceHashAlgorithm;
  private String sourceLabel;

  private ArtifactDynamicMetadata() {

  }

  public String getArtifactSerialNum() {
    return artifactSerialNum;
  }

  public String getDocSourceHash() {
    return docSourceHash;
  }

  public String getEdhGuid() {
    return edhGuid;
  }

  public String getGeoLocation() {
    return geoLocation;
  }

  public String getProvenanceId() {
    return provenanceId;
  }

  public String getSourceHashAlgorithm() {
    return sourceHashAlgorithm;
  }

  public String getSourceLabel() {
    return sourceLabel;
  }

  public static Builder newBuilder() {
    return new Builder();
  }

  void addMutations(Mutation mutation) {
    MutationHelpers.putIfNotNull(mutation, COLUMN_FAMILY_NAME, COLUMN_ARTIFACT_SERIAL_NUM, getArtifactSerialNum());
    MutationHelpers.putIfNotNull(mutation, COLUMN_FAMILY_NAME, COLUMN_DOC_SOURCE_HASH, getDocSourceHash());
    MutationHelpers.putIfNotNull(mutation, COLUMN_FAMILY_NAME, COLUMN_EDH_GUID, getEdhGuid());
    MutationHelpers.putIfNotNull(mutation, COLUMN_FAMILY_NAME, COLUMN_GEO_LOCATION, getGeoLocation());
    MutationHelpers.putIfNotNull(mutation, COLUMN_FAMILY_NAME, COLUMN_PROVENANCE_ID, getProvenanceId());
    MutationHelpers.putIfNotNull(mutation, COLUMN_FAMILY_NAME, COLUMN_SOURCE_HASH_ALGORITHM, getSourceHashAlgorithm());
    MutationHelpers.putIfNotNull(mutation, COLUMN_FAMILY_NAME, COLUMN_SOURCE_LABEL, getSourceLabel());
  }

  public static class Builder {
    private ArtifactDynamicMetadata artifactDynamicMetadata = new ArtifactDynamicMetadata();

    private Builder() {

    }

    public ArtifactDynamicMetadata build() {
      return this.artifactDynamicMetadata;
    }

    public Builder artifactSerialNum(String artifactSerialNum) {
      this.artifactDynamicMetadata.artifactSerialNum = artifactSerialNum;
      return this;
    }

    public Builder docSourceHash(String docSourceHash) {
      this.artifactDynamicMetadata.docSourceHash = docSourceHash;
      return this;
    }

    public Builder edhGuid(String edhGuid) {
      this.artifactDynamicMetadata.edhGuid = edhGuid;
      return this;
    }

    public Builder geoLocation(String geoLocation) {
      this.artifactDynamicMetadata.geoLocation = geoLocation;
      return this;
    }

    public Builder provenanceId(String provenanceId) {
      this.artifactDynamicMetadata.provenanceId = provenanceId;
      return this;
    }

    public Builder sourceHashAlgorithm(String sourceHashAlgorithm) {
      this.artifactDynamicMetadata.sourceHashAlgorithm = sourceHashAlgorithm;
      return this;
    }

    public Builder sourceLabel(String sourceLabel) {
      this.artifactDynamicMetadata.sourceLabel = sourceLabel;
      return this;
    }

    public static void populateFromColumn(ArtifactDynamicMetadata dynamicMetadata, Map.Entry<Key, Value> column) {
      String columnQualifier = column.getKey().getColumnQualifier().toString();
      if (COLUMN_ARTIFACT_SERIAL_NUM.equals(columnQualifier)) {
        dynamicMetadata.artifactSerialNum = column.getValue().toString();
      } else if (COLUMN_DOC_SOURCE_HASH.equals(columnQualifier)) {
        dynamicMetadata.docSourceHash = column.getValue().toString();
      } else if (COLUMN_EDH_GUID.equals(columnQualifier)) {
        dynamicMetadata.edhGuid = column.getValue().toString();
      } else if (COLUMN_GEO_LOCATION.equals(columnQualifier)) {
        dynamicMetadata.geoLocation = column.getValue().toString();
      } else if (COLUMN_PROVENANCE_ID.equals(columnQualifier)) {
        dynamicMetadata.provenanceId = column.getValue().toString();
      } else if (COLUMN_SOURCE_HASH_ALGORITHM.equals(columnQualifier)) {
        dynamicMetadata.sourceHashAlgorithm = column.getValue().toString();
      } else if (COLUMN_SOURCE_LABEL.equals(columnQualifier)) {
        dynamicMetadata.sourceLabel = column.getValue().toString();
      }
    }
  }
}
