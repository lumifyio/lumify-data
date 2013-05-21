package com.altamiracorp.reddawn.ucd.models;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.Date;
import java.util.GregorianCalendar;

import static org.junit.Assert.assertEquals;

@RunWith(JUnit4.class)
public class SourceTest {
  @Test
  public void fail() {
    assertEquals(1, 0);
  }

  @Test
  public void createASource() {
    Source.Builder sb = Source.newBuilder();

    SourceMetadata.Builder sm = SourceMetadata.newBuilder();
    sm.acronym("DOD");
    sm.additionalDetails("additionalDetails");
    sm.country("country");
    sm.dataType("dataType");
    sm.description("description");
    sm.ingestDate(new GregorianCalendar(2013, 1, 1, 1, 1).getTime().getTime());
    sm.ingestStatus("ingestStatus");
    sm.intelTypes("intelTypes");
    sm.location("location");
    sm.mediaTypes("mediaTypes");
    sm.name("name");
    sm.orgAcronym("orgAcronym");
    sm.orgName("orgName");
    sm.orgParentAcronym("orgParentAcronym");
    sm.orgParentName("orgParentName");
    sm.type("type");

    Source source = sb
            .uuid("uuid")
            .sourceMetadata(sm.build())
            .build();

    assertEquals("uuid", source.getKey().toString());
    assertEquals("DOD", source.getMetadata().getAcronym());
    assertEquals("additionalDetails", source.getMetadata().getAdditionalDetails());
    assertEquals("country", source.getMetadata().getCountry());
    assertEquals("dataType", source.getMetadata().getDataType());
    assertEquals("description", source.getMetadata().getDescription());
    assertEquals(new GregorianCalendar(2013, 1, 1, 1, 1).getTime().getTime(), source.getMetadata().getIngestDate());
    assertEquals("ingestStatus", source.getMetadata().getIngestStatus());
    assertEquals("intelTypes", source.getMetadata().getIntelTypes());
    assertEquals("location", source.getMetadata().getLocation());
    assertEquals("mediaTypes", source.getMetadata().getMediaTypes());
    assertEquals("name", source.getMetadata().getName());
    assertEquals("orgAcronym", source.getMetadata().getOrgAcronym());
    assertEquals("orgName", source.getMetadata().getOrgName());
    assertEquals("orgParentAcronym", source.getMetadata().getOrgParentAcronym());
    assertEquals("orgParentName", source.getMetadata().getOrgParentName());
    assertEquals("type", source.getMetadata().getType());
  }
}
