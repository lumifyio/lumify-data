package com.altamiracorp.redDawn.ucd.models;

import com.altamiracorp.redDawn.ucd.models.Artifact;
import org.apache.accumulo.core.data.ColumnUpdate;
import org.apache.accumulo.core.data.Mutation;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.fail;

@RunWith(JUnit4.class)
public class ArtifactTest {
  @Test
  public void testGetUpdates() {
    Calendar cal = Calendar.getInstance();
    cal.set(2013, 4, 3, 11, 12, 0);

    Artifact artifact = new Artifact();
    artifact.setFullFileName("testGetUpdates.txt");
    artifact.setData("testGetUpdates");
    artifact.setExtractedText("testGetUpdatesText");
    artifact.setDocumentDateTime(cal.getTime());
    artifact.setSubject("subject");
    Mutation mutation = artifact.getMutation();

    List<ColumnUpdate> updates = mutation.getUpdates();
    assertMutationContatins(updates, Artifact.COLUMN_FAMILY_CONTENT, Artifact.COLUMN_CONTENT_DOC_ARTIFACT_BYTES, "testGetUpdates");
    assertMutationContatins(updates, Artifact.COLUMN_FAMILY_CONTENT, Artifact.COLUMN_CONTENT_DOC_EXTRACTED_TEXT, "testGetUpdatesText");
    assertMutationContatins(updates, Artifact.COLUMN_FAMILY_GENERIC_METADATA, Artifact.COLUMN_GENERIC_METADATA_FILE_NAME, "testGetUpdates");
    assertMutationContatins(updates, Artifact.COLUMN_FAMILY_GENERIC_METADATA, Artifact.COLUMN_GENERIC_METADATA_FILE_EXTENSION, "txt");
    assertMutationContatins(updates, Artifact.COLUMN_FAMILY_GENERIC_METADATA, Artifact.COLUMN_GENERIC_METADATA_DOCUMENT_DTG, "031112Z MAY 13");
    assertMutationContatins(updates, Artifact.COLUMN_FAMILY_GENERIC_METADATA, Artifact.COLUMN_GENERIC_METADATA_SUBJECT, "subject");
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
