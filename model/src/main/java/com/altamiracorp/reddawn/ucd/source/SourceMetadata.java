package com.altamiracorp.reddawn.ucd.source;

import com.altamiracorp.reddawn.model.ColumnFamily;
import com.altamiracorp.reddawn.model.Value;

public class SourceMetadata extends ColumnFamily {
    public static final String NAME = "Metadata";
    public static final String ACRONYM = "acronym";
    public static final String ADDITIONAL_DETAILS = "additional_details";
    public static final String COUNTRY = "country";
    public static final String DATA_TYPE = "data_type";
    public static final String DESCRIPTION = "description";
    public static final String INGEST_DATE = "ingest_date";
    public static final String INGEST_STATUS = "ingest_status";
    public static final String INTEL_TYPES = "intel_types";
    public static final String LOCATION = "location";
    public static final String MEDIA_TYPES = "media_types";
    public static final String ORG_ACRONYM = "org_acronym";
    public static final String ORG_NAME = "org_name";
    public static final String ORG_PARENT_ACRONYM = "org_parent_acronym";
    public static final String ORG_PARENT_NAME = "org_parent_name";
    public static final String TYPE = "type";

    public SourceMetadata() {
        super(NAME);
    }

    public String getAcronym() {
        return Value.toString(get(ACRONYM));
    }

    public SourceMetadata setAcronym(String acronym) {
        set(ACRONYM, acronym);
        return this;
    }

    public String getAdditionalDetails() {
        return Value.toString(get(ADDITIONAL_DETAILS));
    }

    public SourceMetadata setAdditionalDetails(String additionalDetails) {
        set(ADDITIONAL_DETAILS, additionalDetails);
        return this;
    }

    public String getCountry() {
        return Value.toString(get(COUNTRY));
    }

    public SourceMetadata setCountry(String country) {
        set(COUNTRY, country);
        return this;
    }

    public String getDataType() {
        return Value.toString(get(DATA_TYPE));
    }

    public SourceMetadata setDataType(String dataType) {
        set(DATA_TYPE, dataType);
        return this;
    }

    public String getDescription() {
        return Value.toString(get(DESCRIPTION));
    }

    public SourceMetadata setDescription(String description) {
        set(DESCRIPTION, description);
        return this;
    }

    public Long getIngestDate() {
        return Value.toLong(get(INGEST_DATE));
    }

    public SourceMetadata setIngestDate(Long ingestDate) {
        set(INGEST_DATE, ingestDate);
        return this;
    }

    public String getIngestStatus() {
        return Value.toString(get(INGEST_STATUS));
    }

    public SourceMetadata setIngestStatus(String ingestStatus) {
        set(INGEST_STATUS, ingestStatus);
        return this;
    }

    public String getIntelTypes() {
        return Value.toString(get(INTEL_TYPES));
    }

    public SourceMetadata setIntelTypes(String intelTypes) {
        set(INTEL_TYPES, intelTypes);
        return this;
    }

    public String getLocation() {
        return Value.toString(get(LOCATION));
    }

    public SourceMetadata setLocation(String location) {
        set(LOCATION, location);
        return this;
    }

    public String getMediaTypes() {
        return Value.toString(get(MEDIA_TYPES));
    }

    public SourceMetadata setMediaTypes(String mediaTypes) {
        set(MEDIA_TYPES, mediaTypes);
        return this;
    }

    public String getName() {
        return Value.toString(get(NAME));
    }

    public SourceMetadata setName(String name) {
        set(NAME, name);
        return this;
    }

    public String getOrgAcronym() {
        return Value.toString(get(ORG_ACRONYM));
    }

    public SourceMetadata setOrgAcronym(String orgAcronym) {
        set(ORG_ACRONYM, orgAcronym);
        return this;
    }

    public String getOrgName() {
        return Value.toString(get(ORG_NAME));
    }

    public SourceMetadata setOrgName(String orgName) {
        set(ORG_NAME, orgName);
        return this;
    }

    public String getOrgParentAcronym() {
        return Value.toString(get(ORG_PARENT_ACRONYM));
    }

    public SourceMetadata setOrgParentAcronym(String orgParentAcronym) {
        set(ORG_PARENT_ACRONYM, orgParentAcronym);
        return this;
    }

    public String getOrgParentName() {
        return Value.toString(get(ORG_PARENT_NAME));
    }

    public SourceMetadata setOrgParentName(String orgParentName) {
        set(ORG_PARENT_NAME, orgParentName);
        return this;
    }

    public String getType() {
        return Value.toString(get(TYPE));
    }

    public SourceMetadata setType(String type) {
        set(TYPE, type);
        return this;
    }
}
