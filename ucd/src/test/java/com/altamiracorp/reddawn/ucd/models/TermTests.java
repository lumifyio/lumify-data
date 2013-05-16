package com.altamiracorp.reddawn.ucd.models;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static org.junit.Assert.assertEquals;

@RunWith(JUnit4.class)
public class TermTests {
  @Test
  public void createATerm() {
    TermKey termKey = TermKey.newBuilder()
        .sign("testSign")
        .concept("testConcept")
        .model("UCD")
        .build();

    TermMetadata termMetadata = TermMetadata.newBuilder()
        .artifactKey("testArtifactKey")
        .artifactKeySign("testArtifactKeySign")
        .author("testAuthor")
        .mention(new TermMention(0, 5))
        .build();

    Term term = Term.newBuilder()
        .key(termKey)
        .metadata(termMetadata)
        .build();

    assertEquals("testSign\u001FUCD\u001FtestConcept", term.getKey().toString());

    assertEquals("testArtifactKey", term.getMetadata().getArtifactKey());
    assertEquals("testArtifactKeySign", term.getMetadata().getArtifactKeySign());
    assertEquals("testAuthor", term.getMetadata().getAuthor());
    assertEquals("{\"start\":0,\"end\":5}", term.getMetadata().getMention().toString());
  }
}
