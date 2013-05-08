package com.altamiracorp.reddawn.ucd.models;

import org.apache.accumulo.core.data.ColumnUpdate;
import org.apache.accumulo.core.data.Mutation;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.Calendar;
import java.util.List;

import static com.altamiracorp.reddawn.ucd.models.MutationTestHelpers.assertMutationContains;
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
    assertMutationContains(updates, Artifact.COLUMN_FAMILY_CONTENT, Artifact.COLUMN_CONTENT_DOC_ARTIFACT_BYTES, "testGetUpdates");
    assertMutationContains(updates, Artifact.COLUMN_FAMILY_CONTENT, Artifact.COLUMN_CONTENT_DOC_EXTRACTED_TEXT, "testGetUpdatesText");
    assertMutationContains(updates, Artifact.COLUMN_FAMILY_GENERIC_METADATA, Artifact.COLUMN_GENERIC_METADATA_FILE_NAME, "testGetUpdates");
    assertMutationContains(updates, Artifact.COLUMN_FAMILY_GENERIC_METADATA, Artifact.COLUMN_GENERIC_METADATA_FILE_EXTENSION, "txt");
    assertMutationContains(updates, Artifact.COLUMN_FAMILY_GENERIC_METADATA, Artifact.COLUMN_GENERIC_METADATA_DOCUMENT_DTG, "031112Z MAY 13");
    assertMutationContains(updates, Artifact.COLUMN_FAMILY_GENERIC_METADATA, Artifact.COLUMN_GENERIC_METADATA_SUBJECT, "subject");
  }
}
