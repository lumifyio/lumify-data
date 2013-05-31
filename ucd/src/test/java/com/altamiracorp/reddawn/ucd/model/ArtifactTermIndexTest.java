package com.altamiracorp.reddawn.ucd.model;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(JUnit4.class)
public class ArtifactTermIndexTest {
  @Test
  public void createAnArtifactTermIndex() {
    ArtifactTermIndex.Builder a = ArtifactTermIndex.newBuilder();

    ArtifactKey artifactKey = new ArtifactKey("artifact1");

    ArtifactTermIndex artifactTermIndex = a
        .artifactKey(artifactKey)
        .termMention(new TermKey("termRow1\u001Ftest\u001Fperson"), "termMention1")
        .termMention(new TermKey("termRow1\u001Ftest\u001Fperson"), "termMention2")
        .build();

    assertEquals("artifact1", artifactTermIndex.getKey().toString());

    Map<TermKey, List<String>> termMentions = artifactTermIndex.getTermMentions();
    assertEquals(1, termMentions.keySet().size());
    assertTrue("'termRow1' not found", termMentions.containsKey(new TermKey("termRow1\u001Ftest\u001Fperson")));

    List<String> mentions = termMentions.get(new TermKey("termRow1\u001Ftest\u001Fperson"));
    Collections.sort(mentions);

    assertEquals(2, mentions.size());
    assertEquals("termMention1", mentions.get(0));
    assertEquals("termMention2", mentions.get(1));
  }
}
