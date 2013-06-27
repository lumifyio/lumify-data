package com.altamiracorp.reddawn.ucd.artifactTermIndex;

import com.altamiracorp.reddawn.model.ColumnFamily;
import com.altamiracorp.reddawn.model.MockSession;
import com.altamiracorp.reddawn.model.Row;
import com.altamiracorp.reddawn.model.RowKey;
import com.altamiracorp.reddawn.ucd.term.TermMention;
import com.altamiracorp.reddawn.ucd.term.TermRowKey;
import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(JUnit4.class)
public class ArtifactTermIndexRepositoryTest {
    private MockSession session;
    private ArtifactTermIndexRepository artifactTermIndexRepository;

    @Before
    public void before() {
        session = new MockSession();
        session.initializeTables();
        artifactTermIndexRepository = new ArtifactTermIndexRepository();
    }

    @Test
    public void testFindByRowKey() {
        String rowKeyString = "testArtifactTermIndexId";
        Row<RowKey> row = new Row<RowKey>(ArtifactTermIndex.TABLE_NAME, new RowKey(rowKeyString));

        TermRowKey term1RowKey = new TermRowKey("testSign1", "testModel1", "testConcept1");
        ColumnFamily term1ColumnFamily = new ColumnFamily(term1RowKey.toString());
        term1ColumnFamily
                .set("testTermMention1", "")
                .set("testTermMention2", "");
        row.addColumnFamily(term1ColumnFamily);

        TermRowKey term2RowKey = new TermRowKey("testSign2", "testModel2", "testConcept2");
        ColumnFamily term2ColumnFamily = new ColumnFamily(term2RowKey.toString());
        term2ColumnFamily
                .set("testTermMention3", "")
                .set("testTermMention4", "");
        row.addColumnFamily(term2ColumnFamily);

        session.tables.get(ArtifactTermIndex.TABLE_NAME).add(row);

        ArtifactTermIndex artifactTermIndex = artifactTermIndexRepository.findByRowKey(session, rowKeyString);
        assertEquals(rowKeyString, artifactTermIndex.getRowKey().toString());
        assertEquals(2, artifactTermIndex.getColumnFamilies().size());

        ColumnFamily foundTerm1ColumnFamily = artifactTermIndex.get(term1RowKey.toString());
        assertNotNull(term1RowKey.toString(), foundTerm1ColumnFamily);
        Assert.assertEquals(2, foundTerm1ColumnFamily.getColumns().size());
        assertEquals("", foundTerm1ColumnFamily.get("testTermMention1").toString());
        assertEquals("", foundTerm1ColumnFamily.get("testTermMention2").toString());

        ColumnFamily foundTerm2ColumnFamily = artifactTermIndex.get(term2RowKey.toString());
        assertNotNull(term2RowKey.toString(), foundTerm2ColumnFamily);
        Assert.assertEquals(2, foundTerm2ColumnFamily.getColumns().size());
        assertEquals("", foundTerm2ColumnFamily.get("testTermMention3").toString());
        assertEquals("", foundTerm2ColumnFamily.get("testTermMention4").toString());
    }

    @Test
    public void testSave() {
        ArtifactTermIndex artifactTermIndex = new ArtifactTermIndex("testArtifactId");

        TermMention termMention1 = new TermMention()
                .setArtifactKey("testArtifactId")
                .setMentionStart(111L)
                .setMentionEnd(222L);
        TermMention termMention2 = new TermMention()
                .setArtifactKey("testArtifactId")
                .setMentionStart(333L)
                .setMentionEnd(444L);
        TermMention termMention3 = new TermMention()
                .setArtifactKey("testArtifactId")
                .setMentionStart(555L)
                .setMentionEnd(666L);

        TermRowKey term1RowKey = new TermRowKey("testSign1", "testModel1", "testConcept1");

        artifactTermIndex.addTermMention(term1RowKey, termMention1);
        artifactTermIndex.addTermMention(term1RowKey, termMention2);

        TermRowKey term2RowKey = new TermRowKey("testSign2", "testModel2", "testConcept2");
        artifactTermIndex.addTermMention(term2RowKey, termMention3);

        artifactTermIndexRepository.save(session, artifactTermIndex);

        assertEquals(1, session.tables.get(ArtifactTermIndex.TABLE_NAME).size());
        Row row = session.tables.get(ArtifactTermIndex.TABLE_NAME).get(0);
        assertEquals("testArtifactId", row.getRowKey().toString());

        assertEquals(2, row.getColumnFamilies().size());

        ColumnFamily term1ColumnFamily = row.get(term1RowKey.toString());
        assertEquals(term1RowKey.toString(), term1ColumnFamily.getColumnFamilyName());
        assertEquals(2, term1ColumnFamily.getColumns().size());
        assertEquals("", term1ColumnFamily.get(termMention1.getColumnFamilyName()).toString());
        assertEquals("", term1ColumnFamily.get(termMention2.getColumnFamilyName()).toString());

        ColumnFamily term2ColumnFamily = row.get(term2RowKey.toString());
        assertEquals(term2RowKey.toString(), term2ColumnFamily.getColumnFamilyName());
        assertEquals(1, term2ColumnFamily.getColumns().size());
        assertEquals("", term2ColumnFamily.get(termMention3.getColumnFamilyName()).toString());
    }
}
