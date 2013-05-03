package com.altamiracorp.redDawn.ucd.models;

import com.altamiracorp.redDawn.ucd.models.Artifact;
import org.apache.accumulo.core.data.ColumnUpdate;
import org.apache.accumulo.core.data.Mutation;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.List;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.fail;

@RunWith(JUnit4.class)
public class ArtifactTest {
  @Test
  public void testGetUpdates() {
    Artifact artifact = new Artifact();
    artifact.setFullFileName("testGetUpdates.txt");
    artifact.setData("testGetUpdates");
    Mutation mutation = artifact.getMutation();

    List<ColumnUpdate> updates = mutation.getUpdates();
    assertMutationContatins(updates, Artifact.COLUMN_FAMILY_CONTENT, Artifact.COLUMN_CONTENT_DOC_ARTIFACT_BYTES, "testGetUpdates");
  }

  private void assertMutationContatins(List<ColumnUpdate> updates, String columnFamily, String columnQualifier, String value) {
    for (ColumnUpdate update : updates) {
      if (new String(update.getColumnFamily()).equals(columnFamily)
          && new String(update.getColumnQualifier()).equals(columnQualifier)) {
        assertEquals(value, new String(update.getValue()));
        return;
      }
    }
    fail("Could not find update: " + columnFamily + ":" + columnQualifier + " = " + value);
  }
}
