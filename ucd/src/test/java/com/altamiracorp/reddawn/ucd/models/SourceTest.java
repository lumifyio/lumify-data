package com.altamiracorp.reddawn.ucd.models;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static org.junit.Assert.assertEquals;

@RunWith(JUnit4.class)
public class SourceTest {
  @Test
  public void createASource() {
    Source.Builder sb = Source.newBuilder();

    SourceMetadata.Builder sm = SourceMetadata.newBuilder();
    sm.acronym("DOD");

    Source source = sb
            .uuid("uuid")
            .sourceMetadata(sm.build())
            .build();

    String uuid = source.getKey().toString();
    assertEquals("uuid", uuid);
  }
}
