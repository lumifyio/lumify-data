package com.altamiracorp.reddawn.ucd.term;

import com.altamiracorp.reddawn.model.*;
import com.altamiracorp.reddawn.ucd.artifactTermIndex.ArtifactTermIndex;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(JUnit4.class)
public class TermRepositoryTest {
    private MockSession session;
    private TermRepository termRepository;

    @Before
    public void before() {
        session = new MockSession();
        session.initializeTables();
        termRepository = new TermRepository();
    }

    @Test
    public void testFindByRowKey() {
        String rowKeyString = RowKeyHelper.build("testSign", "testModelKey", "testConceptLabel");
        Row<RowKey> row = new Row<RowKey>(Term.TABLE_NAME, new RowKey(rowKeyString));

        ColumnFamily termMentionColumnFamily1 = new ColumnFamily(RowKeyHelper.buildSHA256KeyString("mention1".getBytes()));
        termMentionColumnFamily1
                .set(TermMention.ARTIFACT_KEY, "testArtifactKey1")
                .set(TermMention.ARTIFACT_KEY_SIGN, "testArtifactKeySign1")
                .set(TermMention.AUTHOR, "testAuthor1")
                .set(TermMention.GEO_LOCATION, "testGeoLocation1")
                .set(TermMention.MENTION, "testMention1")
                .set(TermMention.PROVENANCE_ID, "testProvenanceId1")
                .set(TermMention.SECURITY_MARKING, "testSecurityMarking1")
                .set(TermMention.DATE, 111L)
                .set("extra1", "textExtra1");
        row.addColumnFamily(termMentionColumnFamily1);

        ColumnFamily termMentionColumnFamily2 = new ColumnFamily(RowKeyHelper.buildSHA256KeyString("mention2".getBytes()));
        termMentionColumnFamily2
                .set(TermMention.ARTIFACT_KEY, "testArtifactKey2")
                .set(TermMention.ARTIFACT_KEY_SIGN, "testArtifactKeySign2")
                .set(TermMention.AUTHOR, "testAuthor2")
                .set(TermMention.GEO_LOCATION, "testGeoLocation2")
                .set(TermMention.MENTION, "testMention2")
                .set(TermMention.PROVENANCE_ID, "testProvenanceId2")
                .set(TermMention.SECURITY_MARKING, "testSecurityMarking2")
                .set(TermMention.DATE, 222L)
                .set("extra2", "textExtra2");
        row.addColumnFamily(termMentionColumnFamily2);

        ColumnFamily extraColumnFamily = new ColumnFamily("testExtraColumnFamily");
        extraColumnFamily
                .set("testExtraColumn", "testExtraValue");
        row.addColumnFamily(extraColumnFamily);

        session.tables.get(Term.TABLE_NAME).add(row);

        Term term = termRepository.findByRowKey(session, rowKeyString);
        assertEquals(rowKeyString, term.getRowKey().toString());
        assertEquals(3, term.getColumnFamilies().size());

        assertEquals(2, term.getTermMentions().size());

        TermMention termMention1 = term.get(RowKeyHelper.buildSHA256KeyString("mention1".getBytes()));
        assertEquals(RowKeyHelper.buildSHA256KeyString("mention1".getBytes()), termMention1.getColumnFamilyName());
        assertEquals("testArtifactKey1", termMention1.getArtifactKey());
        assertEquals("testArtifactKeySign1", termMention1.getArtifactKeySign());
        assertEquals("testAuthor1", termMention1.getAuthor());
        assertEquals("testGeoLocation1", termMention1.getGeoLocation());
        assertEquals("testMention1", termMention1.getMention());
        assertEquals("testProvenanceId1", termMention1.getProvenanceId());
        assertEquals("testSecurityMarking1", termMention1.getSecurityMarking());
        assertEquals(111L, termMention1.getDate().longValue());
        assertEquals("textExtra1", termMention1.get("extra1").toString());

        TermMention termMention2 = term.get(RowKeyHelper.buildSHA256KeyString("mention2".getBytes()));
        assertEquals(RowKeyHelper.buildSHA256KeyString("mention2".getBytes()), termMention2.getColumnFamilyName());
        assertEquals("testArtifactKey2", termMention2.getArtifactKey());
        assertEquals("testArtifactKeySign2", termMention2.getArtifactKeySign());
        assertEquals("testAuthor2", termMention2.getAuthor());
        assertEquals("testGeoLocation2", termMention2.getGeoLocation());
        assertEquals("testMention2", termMention2.getMention());
        assertEquals("testProvenanceId2", termMention2.getProvenanceId());
        assertEquals("testSecurityMarking2", termMention2.getSecurityMarking());
        assertEquals(222L, termMention2.getDate().longValue());
        assertEquals("textExtra2", termMention2.get("extra2").toString());

        ColumnFamily foundExtraColumnFamily = term.get("testExtraColumnFamily");
        assertNotNull("foundExtraColumnFamily", foundExtraColumnFamily);
        assertEquals("testExtraValue", foundExtraColumnFamily.get("testExtraColumn").toString());
    }

    @Test
    public void testSave() {
        Term term = new Term("testSign", "testModelKey", "testConceptLabel");

        TermMention termMention1 = new TermMention()
                .setArtifactKey("testArtifactKey1")
                .setArtifactKeySign("testArtifactKeySign1")
                .setAuthor("testAuthor1")
                .setGeoLocation("testGeoLocation1")
                .setMention("testMention1")
                .setProvenanceId("testProvenanceId1")
                .setSecurityMarking("testSecurityMarking1")
                .setDate(111L);
        termMention1.set("testExtra1", "testExtra1Value");
        term.addTermMention(termMention1);

        TermMention termMention2 = new TermMention()
                .setArtifactKey("testArtifactKey2")
                .setArtifactKeySign("testArtifactKeySign2")
                .setAuthor("testAuthor2")
                .setGeoLocation("testGeoLocation2")
                .setMention("testMention2")
                .setProvenanceId("testProvenanceId2")
                .setSecurityMarking("testSecurityMarking2")
                .setDate(222L);
        termMention2.set("testExtra2", "testExtra2Value");
        term.addTermMention(termMention2);

        term.addColumnFamily(
                new ColumnFamily("testExtraColumnFamily")
                        .set("testExtraColumn", "testExtraValue"));

        termRepository.save(session, term);

        assertEquals(1, session.tables.get(Term.TABLE_NAME).size());
        Row row = session.tables.get(Term.TABLE_NAME).get(0);
        assertEquals(RowKeyHelper.build("testsign", "testModelKey", "testConceptLabel"), row.getRowKey().toString());

        assertEquals(3, row.getColumnFamilies().size());

        ColumnFamily termMentionColumnFamily1 = row.get("urn\u001Fsha256\u001F272f17747be67d6f6f909766868fdf010876458c619c4aea934a089cb0a92235");
        assertEquals("urn\u001Fsha256\u001F272f17747be67d6f6f909766868fdf010876458c619c4aea934a089cb0a92235", termMentionColumnFamily1.getColumnFamilyName());
        assertEquals("testArtifactKey1", termMentionColumnFamily1.get(TermMention.ARTIFACT_KEY).toString());
        assertEquals("testArtifactKeySign1", termMentionColumnFamily1.get(TermMention.ARTIFACT_KEY_SIGN).toString());
        assertEquals("testAuthor1", termMentionColumnFamily1.get(TermMention.AUTHOR).toString());
        assertEquals("testGeoLocation1", termMentionColumnFamily1.get(TermMention.GEO_LOCATION).toString());
        assertEquals("testMention1", termMentionColumnFamily1.get(TermMention.MENTION).toString());
        assertEquals("testProvenanceId1", termMentionColumnFamily1.get(TermMention.PROVENANCE_ID).toString());
        assertEquals("testSecurityMarking1", termMentionColumnFamily1.get(TermMention.SECURITY_MARKING).toString());
        assertEquals(111L, termMentionColumnFamily1.get(TermMention.DATE).toLong().longValue());
        assertEquals("testExtra1Value", termMentionColumnFamily1.get("testExtra1").toString());

        ColumnFamily termMentionColumnFamily2 = row.get("urn\u001Fsha256\u001F0d293c6aae2ea3e016c445fc7bb581525bb666ad103f3e9c1ed2f3a3d2919767");
        assertEquals("urn\u001Fsha256\u001F0d293c6aae2ea3e016c445fc7bb581525bb666ad103f3e9c1ed2f3a3d2919767", termMentionColumnFamily2.getColumnFamilyName());
        assertEquals("testArtifactKey2", termMentionColumnFamily2.get(TermMention.ARTIFACT_KEY).toString());
        assertEquals("testArtifactKeySign2", termMentionColumnFamily2.get(TermMention.ARTIFACT_KEY_SIGN).toString());
        assertEquals("testAuthor2", termMentionColumnFamily2.get(TermMention.AUTHOR).toString());
        assertEquals("testGeoLocation2", termMentionColumnFamily2.get(TermMention.GEO_LOCATION).toString());
        assertEquals("testMention2", termMentionColumnFamily2.get(TermMention.MENTION).toString());
        assertEquals("testProvenanceId2", termMentionColumnFamily2.get(TermMention.PROVENANCE_ID).toString());
        assertEquals("testSecurityMarking2", termMentionColumnFamily2.get(TermMention.SECURITY_MARKING).toString());
        assertEquals(222L, termMentionColumnFamily2.get(TermMention.DATE).toLong().longValue());
        assertEquals("testExtra2Value", termMentionColumnFamily2.get("testExtra2").toString());

        ColumnFamily extraColumnFamily = row.get("testExtraColumnFamily");
        assertNotNull("extraColumnFamily", extraColumnFamily);
        assertEquals(1, extraColumnFamily.getColumns().size());
        assertEquals("testExtraValue", extraColumnFamily.get("testExtraColumn").toString());

        assertEquals(2, session.tables.get(ArtifactTermIndex.TABLE_NAME).size());

        Row artifactTermIndex1Row = session.tables.get(ArtifactTermIndex.TABLE_NAME).get(0);
        assertEquals("testArtifactKey1", artifactTermIndex1Row.getRowKey().toString());
        assertEquals(1, artifactTermIndex1Row.getColumnFamilies().size());
        List<ColumnFamily> columnFamilyList = new ArrayList<ColumnFamily>(artifactTermIndex1Row.getColumnFamilies());
        assertEquals(term.getRowKey().toString(), columnFamilyList.get(0).getColumnFamilyName());
        assertEquals(1, columnFamilyList.get(0).getColumns().size());
        assertEquals("", columnFamilyList.get(0).get(termMention1.getColumnFamilyName()).toString());

        Row artifactTermIndex2Row = session.tables.get(ArtifactTermIndex.TABLE_NAME).get(1);
        assertEquals("testArtifactKey2", artifactTermIndex2Row.getRowKey().toString());
        assertEquals(1, artifactTermIndex2Row.getColumnFamilies().size());
        columnFamilyList = new ArrayList<ColumnFamily>(artifactTermIndex2Row.getColumnFamilies());
        assertEquals(term.getRowKey().toString(), columnFamilyList.get(0).getColumnFamilyName());
        assertEquals(1, columnFamilyList.get(0).getColumns().size());
        assertEquals("", columnFamilyList.get(0).get(termMention2.getColumnFamilyName()).toString());
    }
}
