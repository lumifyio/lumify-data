package com.altamiracorp.reddawn.ucd.models;

import org.apache.accumulo.core.data.ColumnUpdate;
import org.apache.accumulo.core.data.Mutation;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.List;

import static com.altamiracorp.reddawn.ucd.models.AccumuloTestHelpers.assertMutationContains;

@RunWith(JUnit4.class)
public class TermTest {
  @Test
  public void testGetUpdates() {
    Term term = new Term();
    term.setSign("joe");
    term.setModelKey("reddawn");
    term.setConceptLabel("PERSON");

    TermMention termMention = new TermMention();
    termMention.setArtifactKey("urn:sha256:123456");
    termMention.setMention("{\"start\":1234,\"end\":2345}");
    term.addTermMention(termMention);

    Mutation mutation = term.getMutation();

    List<ColumnUpdate> updates = mutation.getUpdates();
    assertMutationContains(updates, termMention.getRowId(), TermMention.COLUMN_ARTIFACT_KEY, "urn:sha256:123456");
    assertMutationContains(updates, termMention.getRowId(), TermMention.COLUMN_MENTION, "{\"start\":1234,\"end\":2345}");
  }
}
