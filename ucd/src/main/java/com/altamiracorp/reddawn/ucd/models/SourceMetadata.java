package com.altamiracorp.reddawn.ucd.models;

public class SourceMetadata {
  public static final String COLUMN_FAMILY_NAME = "Source";
  private static final String COLUMN_ACRONYM = "acronym";
  private String acronym;
  private String additionalDetails;
  private String country;
  private String dataType;
  private String description;
  private long ingestDate;
  private String ingestStatus;
  private String intelTypes;
  private String location;
  private String mediaTypes;
  private String name;
  private String orgAcronym;
  private String orgName;
  private String orgParentAcronym;
  private String orgParentName;
  private String type;
          
          
  private SourceMetadata() {

  }

  public static Builder newBuilder() {
    return new Builder();
  }

  public String getAcronym() {
    return acronym;
  }

  public String getAdditionalDetails() {
    return additionalDetails;
  }

  public String getCountry() {
    return country;
  }

  public String getDataType() {
    return dataType;
  }

  public String getDescription() {
    return description;
  }

  public long getIngestDate() {
    return ingestDate;
  }

  public String getIngestStatus() {
    return ingestStatus;
  }

  public String getIntelTypes() {
    return intelTypes;
  }

  public String getLocation() {
    return location;
  }

  public String getMediaTypes() {
    return mediaTypes;
  }

  public String getName() {
    return name;
  }

  public String getOrgAcronym() {
    return orgAcronym;
  }

  public String getOrgName() {
    return orgName;
  }

  public String getOrgParentAcronym() {
    return orgParentAcronym;
  }

  public String getOrgParentName() {
    return orgParentName;
  }

  public String getType() {
    return type;
  }

  public static class Builder {
    private SourceMetadata metadata = new SourceMetadata();

    private Builder() {
    }

    public SourceMetadata build() {
      return this.metadata;
    }

    public void acronym(String acronym) {
      metadata.acronym = acronym;
    }

    public void additionalDetails(String additionalDetails) {
      metadata.additionalDetails = additionalDetails;
    }

    public void country(String country) {
      metadata.country = country;
    }

    public void dataType(String dataType) {
      metadata.dataType = dataType;
    }

    public void description(String description) {
      metadata.description = description;
    }

    public void ingestDate(long ingestDate) {
      metadata.ingestDate = ingestDate;
    }

    public void ingestStatus(String ingestStatus) {
      metadata.ingestStatus = ingestStatus;
    }

    public void intelTypes(String intelTypes) {
      metadata.intelTypes = intelTypes;
    }

    public void location(String location) {
      metadata.location = location;
    }

    public void mediaTypes(String mediaTypes) {
      metadata.mediaTypes = mediaTypes;
    }

    public void name(String name) {
      metadata.name = name;
    }

    public void orgAcronym(String orgAcronym) {
      metadata.orgAcronym = orgAcronym;
    }

    public void orgName(String orgName) {
      metadata.orgName = orgName;
    }

    public void orgParentAcronym(String orgParentAcronym) {
      metadata.orgParentAcronym = orgParentAcronym;
    }

    public void orgParentName(String orgParentName) {
      metadata.orgParentName = orgParentName;
    }

    public void type(String type) {
      metadata.type = type;
    }

  }
}
