package com.altamiracorp.reddawn.ucd.models;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.ArrayList;

import static org.junit.Assert.assertEquals;

@RunWith(JUnit4.class)
public class TermTest {
  @Test
  public void createATerm() {
    TermKey termKey = TermKey.newBuilder()
        .sign("testSign")
        .concept("testConcept")
        .model("UCD")
        .build();

    TermMetadata termMetadata = TermMetadata.newBuilder()
        .artifactKey(new ArtifactKey("testArtifactKey"))
        .artifactKeySign("testArtifactKeySign")
        .author("testAuthor")
        .mention(new TermMention(0, 5))
        .build();

    Term term = Term.newBuilder()
        .key(termKey)
        .metadata(termMetadata)
        .build();

    assertEquals("testSign\u001FUCD\u001FtestConcept", term.getKey().toString());

    TermMetadata termMetadata1 = new ArrayList<TermMetadata>(term.getMetadata()).get(0);
    assertEquals("testArtifactKey", termMetadata1.getArtifactKey().toString());
    assertEquals("testArtifactKeySign", termMetadata1.getArtifactKeySign());
    assertEquals("testAuthor", termMetadata1.getAuthor());
    assertEquals("{\"start\":0,\"end\":5}", termMetadata1.getMention().toString());
  }
}
