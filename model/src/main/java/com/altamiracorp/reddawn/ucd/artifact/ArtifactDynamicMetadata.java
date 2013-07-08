package com.altamiracorp.reddawn.ucd.artifact;

import com.altamiracorp.reddawn.model.ColumnFamily;
import com.altamiracorp.reddawn.model.GeoLocation;
import com.altamiracorp.reddawn.model.Value;
import org.json.JSONException;
import org.json.JSONObject;

public class ArtifactDynamicMetadata extends ColumnFamily {
    public static final String NAME = "Dynamic_Metadata";
    public static final String ARTIFACT_SERIAL_NUMBER = "artifactSerialNumber";
    public static final String ATTACHMENT = "attachment";
    public static final String DOC_SOURCE_HASH = "doc_source_hash";
    public static final String EDH_GUID = "edhGuid";
    public static final String GEO_LOCATION = "geolocation";
    public static final String PROVENANCE_ID = "provenanceId";
    public static final String SOURCE_HASH_ALGORITHM = "source_hash_algorithm";
    public static final String SOURCE_LABEL = "source_label";
    public static final String STRUCTURED_ANNOTATION_OBJECT = "structured_annotation_object";
    public static final String UNSTRUCTURED_ANNOTATION_OBJECT = "unstructured_annotation_object";

    public ArtifactDynamicMetadata() {
        super(NAME);
    }

    public String getArtifactSerialNumber() {
        return Value.toString(get(ARTIFACT_SERIAL_NUMBER));
    }

    public ArtifactDynamicMetadata setArtifactSerialNumber(String artifactSerialNumber) {
        set(ARTIFACT_SERIAL_NUMBER, artifactSerialNumber);
        return this;
    }

    public String getAttachment() {
        return Value.toString(get(ATTACHMENT));
    }

    public ArtifactDynamicMetadata setAttachment(String attachment) {
        set(ATTACHMENT, attachment);
        return this;
    }

    public String getDocSourceHash() {
        return Value.toString(get(DOC_SOURCE_HASH));
    }

    public ArtifactDynamicMetadata setDocSourceHash(String docSourceHash) {
        set(DOC_SOURCE_HASH, docSourceHash);
        return this;
    }

    public String getEdhGuid() {
        return Value.toString(get(EDH_GUID));
    }

    public ArtifactDynamicMetadata setEdhGuid(String edhGuid) {
        set(EDH_GUID, edhGuid);
        return this;
    }

    public String getGeoLocation() {
        return Value.toString(get(GEO_LOCATION));
    }

    public ArtifactDynamicMetadata setGeolocation(String geoLocation) {
        set(GEO_LOCATION, geoLocation);
        return this;
    }

    public Double getLatitude() {
        return GeoLocation.getLatitude(getGeoLocation());
    }

    public Double getLongitude() {
        return GeoLocation.getLongitude(getGeoLocation());
    }

    public String getProvenanceId() {
        return Value.toString(get(PROVENANCE_ID));
    }

    public ArtifactDynamicMetadata setProvenanceId(String provenanceId) {
        set(PROVENANCE_ID, provenanceId);
        return this;
    }

    public String getSourceHashAlgorithm() {
        return Value.toString(get(SOURCE_HASH_ALGORITHM));
    }

    public ArtifactDynamicMetadata setSourceHashAlgorithm(String sourceHashAlgorithm) {
        set(SOURCE_HASH_ALGORITHM, sourceHashAlgorithm);
        return this;
    }

    public String getSourceLabel() {
        return Value.toString(get(SOURCE_LABEL));
    }

    public ArtifactDynamicMetadata setSourceLabel(String sourceLabel) {
        set(SOURCE_LABEL, sourceLabel);
        return this;
    }

    public String getStructuredAnnotationObject() {
        return Value.toString(get(STRUCTURED_ANNOTATION_OBJECT));
    }

    public ArtifactDynamicMetadata setStructuredAnnotationObject(String structuredAnnotationObject) {
        set(STRUCTURED_ANNOTATION_OBJECT, structuredAnnotationObject);
        return this;
    }

    public String getUnstructuredAnnotationObject() {
        return Value.toString(get(UNSTRUCTURED_ANNOTATION_OBJECT));
    }

    public ArtifactDynamicMetadata setUnstructuredAnnotationObject(String unstructuredAnnotationObject) {
        set(UNSTRUCTURED_ANNOTATION_OBJECT, unstructuredAnnotationObject);
        return this;
    }

    @Override
    public JSONObject toJson() {
        try {
            JSONObject json = super.toJson();

            Double latitude = getLatitude();
            if (latitude != null) {
                json.put("latitude", latitude);
            }

            Double longitude = getLongitude();
            if (longitude != null) {
                json.put("longitude", longitude);
            }

            return json;
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }
}
