package com.altamiracorp.reddawn.ucd.sentence;

import com.altamiracorp.reddawn.model.ColumnFamily;
import com.altamiracorp.reddawn.model.MockSession;
import com.altamiracorp.reddawn.model.Row;
import com.altamiracorp.reddawn.model.RowKey;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class SentenceRepositoryTest {
    private MockSession session;
    private SentenceRepository sentenceRepository;

    @Before
    public void before() {
        session = new MockSession();
        session.initializeTables();
        sentenceRepository = new SentenceRepository();
    }

    @Test
    public void sentenceRowKey_GetArtifactRowKeyWithColonInArtifactKey() {
        SentenceRowKey sentenceRowKey = new SentenceRowKey("urn:sha256:007d1437117:0000000000000000:0000000000000000");
        assertEquals("urn:sha256:007d1437117", sentenceRowKey.getArtifactRowKey());
    }

    @Test
    public void sentenceRowKey_GetArtifactRowKeyWithoutColonInArtifactKey() {
        SentenceRowKey sentenceRowKey = new SentenceRowKey("urn:sha256:007d1437117:0000000000000000:0000000000000000");
        assertEquals("urn:sha256:007d1437117", sentenceRowKey.getArtifactRowKey());
    }

    @Test
    public void testFindByRowKey() {
        String rowKeyString = "testArtifactId:0000000000000222:0000000000000111";
        Row<RowKey> row = new Row<RowKey>(Sentence.TABLE_NAME, new RowKey(rowKeyString));

        ColumnFamily sentenceDataColumnFamily = new ColumnFamily(SentenceData.NAME);
        sentenceDataColumnFamily
                .set(SentenceData.ARTIFACT_ID, "testArtifactId")
                .set(SentenceData.START, 111L)
                .set(SentenceData.END, 222L)
                .set(SentenceData.TEXT, "testText")
                .set("extra1", "textExtra1");
        row.addColumnFamily(sentenceDataColumnFamily);

        ColumnFamily sentenceMetadataColumnFamily = new ColumnFamily(SentenceMetadata.NAME);
        sentenceMetadataColumnFamily
                .set(SentenceMetadata.AUTHOR, "testAuthor")
                .set(SentenceMetadata.CONTENT_HASH, "testContentHash".getBytes())
                .set(SentenceMetadata.DATE, 333L)
                .set(SentenceMetadata.EXTRACTOR_ID, "testExtractorId")
                .set(SentenceMetadata.SECURITY_MARKING, "testSecurityMarking")
                .set("extra2", "textExtra2");
        row.addColumnFamily(sentenceMetadataColumnFamily);

        ColumnFamily extraColumnFamily = new ColumnFamily("testExtraColumnFamily");
        extraColumnFamily
                .set("testExtraColumn", "testExtraValue");
        row.addColumnFamily(extraColumnFamily);

        session.tables.get(Sentence.TABLE_NAME).add(row);

        Sentence sentence = sentenceRepository.findByRowKey(session, rowKeyString);
        assertEquals(rowKeyString, sentence.getRowKey().toString());
        assertEquals(3, sentence.getColumnFamilies().size());

        SentenceData sentenceData = sentence.getData();
        assertEquals(SentenceData.NAME, sentenceData.getColumnFamilyName());
        assertEquals("testArtifactId", sentenceData.getArtifactId());
        assertEquals(111L, sentenceData.getStart().longValue());
        assertEquals(222L, sentenceData.getEnd().longValue());
        assertEquals("testText", sentenceData.getText());
        assertEquals("textExtra1", sentenceData.get("extra1").toString());

        SentenceMetadata sentenceMetadata = sentence.getMetadata();
        assertEquals(SentenceMetadata.NAME, sentenceMetadata.getColumnFamilyName());
        assertEquals("testAuthor", sentenceMetadata.getAuthor());
        assertEquals("testContentHash", new String(sentenceMetadata.getContentHash()));
        assertEquals(333L, sentenceMetadata.getDate().longValue());
        assertEquals("testExtractorId", sentenceMetadata.getExtractorId());
        assertEquals("testSecurityMarking", sentenceMetadata.getSecurityMarking());
        assertEquals("textExtra2", sentenceMetadata.get("extra2").toString());

        ColumnFamily foundExtraColumnFamily = sentence.get("testExtraColumnFamily");
        assertNotNull("foundExtraColumnFamily", foundExtraColumnFamily);
        assertEquals("testExtraValue", foundExtraColumnFamily.get("testExtraColumn").toString());
    }

    @Test
    public void testSave() {
        Sentence sentence = new Sentence();

        sentence.getData()
                .setArtifactId("testArtifactId")
                .setStart(1L)
                .setEnd(10L)
                .setText("This is a test")
                .set("testExtra", "testExtraValue1");

        sentence.getMetadata()
                .setAuthor("testAuthor")
                .setContentHash("This is a test")
                .setDate(111L)
                .setExtractorId("testExtractorId")
                .setSecurityMarking("testSecurityMarking")
                .set("testExtra", "testExtraValue2");

        sentence.addColumnFamily(
                new ColumnFamily("testExtraColumnFamily")
                        .set("testExtraColumn", "testExtraValue"));

        sentenceRepository.save(session, sentence);

        assertEquals(1, session.tables.get(Sentence.TABLE_NAME).size());
        Row row = session.tables.get(Sentence.TABLE_NAME).get(0);
        assertEquals("testArtifactId:0000000000000010:0000000000000001", row.getRowKey().toString());

        assertEquals(3, row.getColumnFamilies().size());

        ColumnFamily sentenceDataColumnFamily = row.get(SentenceData.NAME);
        assertEquals(SentenceData.NAME, sentenceDataColumnFamily.getColumnFamilyName());
        assertEquals("testArtifactId", sentenceDataColumnFamily.get(SentenceData.ARTIFACT_ID).toString());
        assertEquals(1L, sentenceDataColumnFamily.get(SentenceData.START).toLong().longValue());
        assertEquals(10L, sentenceDataColumnFamily.get(SentenceData.END).toLong().longValue());
        assertEquals("This is a test", sentenceDataColumnFamily.get(SentenceData.TEXT).toString());
        assertEquals("testExtraValue1", sentenceDataColumnFamily.get("testExtra").toString());

        ColumnFamily sentenceMetadataColumnFamily = row.get(SentenceMetadata.NAME);
        assertEquals(SentenceMetadata.NAME, sentenceMetadataColumnFamily.getColumnFamilyName());
        assertEquals("testAuthor", sentenceMetadataColumnFamily.get(SentenceMetadata.AUTHOR).toString());
        assertEquals("�\u0011NE\u0001�����>\u0017�F�", sentenceMetadataColumnFamily.get(SentenceMetadata.CONTENT_HASH).toString());
        assertEquals(111L, sentenceMetadataColumnFamily.get(SentenceMetadata.DATE).toLong().longValue());
        assertEquals("testExtractorId", sentenceMetadataColumnFamily.get(SentenceMetadata.EXTRACTOR_ID).toString());
        assertEquals("testSecurityMarking", sentenceMetadataColumnFamily.get(SentenceMetadata.SECURITY_MARKING).toString());
        assertEquals("testExtraValue2", sentenceMetadataColumnFamily.get("testExtra").toString());

        ColumnFamily extraColumnFamily = row.get("testExtraColumnFamily");
        assertNotNull("extraColumnFamily", extraColumnFamily);
        assertEquals(1, extraColumnFamily.getColumns().size());
        assertEquals("testExtraValue", extraColumnFamily.get("testExtraColumn").toString());
    }
}
