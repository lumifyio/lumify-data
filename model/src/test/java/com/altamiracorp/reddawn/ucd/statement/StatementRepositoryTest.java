package com.altamiracorp.reddawn.ucd.statement;

import com.altamiracorp.reddawn.model.*;
import com.altamiracorp.reddawn.ucd.artifact.Artifact;
import com.altamiracorp.reddawn.ucd.artifact.ArtifactContent;
import com.altamiracorp.reddawn.ucd.artifact.ArtifactDynamicMetadata;
import com.altamiracorp.reddawn.ucd.artifact.ArtifactGenericMetadata;
import com.altamiracorp.reddawn.ucd.predicate.PredicateRowKey;
import com.altamiracorp.reddawn.ucd.term.Term;
import com.altamiracorp.reddawn.ucd.term.TermRowKey;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

public class StatementRepositoryTest {
    private MockSession session;
    private StatementRepository statementRepository;

    @Before
    public void before() {
        session = new MockSession();
        session.initializeTables();
        statementRepository = new StatementRepository();
    }

    @Test
    public void testStatementRowKey_Typical() {
        StatementRowKey statementRowKey = new StatementRowKey(
                new TermRowKey("Bob", "CTA", "Person"),
                new PredicateRowKey("urn:mil.army.dsc:schema:dataspace", "knows"),
                new TermRowKey("Tina", "CTA", "Person")
        );

        assertEquals("bob\u001FCTA\u001FPerson\u001Eurn:mil.army.dsc:schema:dataspace\u001Fknows\u001Etina\u001FCTA\u001FPerson", statementRowKey.toString());
    }

    @Test
    public void testStatementRowKey_Null() {
        try {
            StatementRowKey statementRowKey = new StatementRowKey(
                    null,
                    null,
                    null
            );
            fail("Expected an exception");
        } catch (NullPointerException ex) {

        }
    }

    @Test
    public void testColumnFamilyNamesNeverChange() {
        String originalValue = "someSha256";
        StatementArtifact statementArtifact = new StatementArtifact(originalValue);
        statementArtifact.set(StatementArtifact.SENTENCE, "somethingElse");
        assertEquals(originalValue, statementArtifact.getColumnFamilyName());
    }

    @Test
    public void testFindByRowKey() {
        // ARRANGE
        Row<RowKey> row = new Row<RowKey>(Statement.TABLE_NAME, new RowKey("jim\u001FCTA\u001FPerson\u001Eurn:mil.army.dsc:schema:dataspace\u001Fknows\u001Etina\u001FCTA\u001FPerson"));

        ColumnFamily artifactColumnFamily = new ColumnFamily("urn:sha256:007d");
        artifactColumnFamily
                .set(StatementArtifact.ARTIFACT_KEY, "testArtifactKey".getBytes())
                .set(StatementArtifact.AUTHOR, "testAuthor".getBytes())
                .set(StatementArtifact.DATE, 100L)
                .set(StatementArtifact.EXTRACTOR_ID, "testExtractorId")
                .set(StatementArtifact.SECURITY_MARKING, "testSecurityMarking")
                .set(StatementArtifact.SENTENCE, "testSentence")
                .set("testStatementContentExtra", "testStatementContentExtraValue");
        row.addColumnFamily(artifactColumnFamily);

        ColumnFamily extraColumnFamily = new ColumnFamily("testExtraColumnFamily");
        extraColumnFamily.set("testExtraColumn", "testExtraValue");
        row.addColumnFamily(extraColumnFamily);

        session.tables.get(Statement.TABLE_NAME).add(row);

        // ACT

        Statement statement = statementRepository.findByRowKey(session, "jim\u001FCTA\u001FPerson\u001Eurn:mil.army.dsc:schema:dataspace\u001Fknows\u001Etina\u001FCTA\u001FPerson");

        // ASSERT

        assertEquals("jim\u001FCTA\u001FPerson\u001Eurn:mil.army.dsc:schema:dataspace\u001Fknows\u001Etina\u001FCTA\u001FPerson", statement.getRowKey().toString());
        StatementArtifact statementArtifacts = statement.getStatementArtifacts().get(0);

        assertEquals("testArtifactKey", statementArtifacts.getArtifactKey());
        assertEquals("testAuthor", statementArtifacts.getAuthor());
        assertEquals((Long)100L, statementArtifacts.getDate());
        assertEquals("testExtractorId", statementArtifacts.getExtractorId());
        assertEquals("testSecurityMarking", statementArtifacts.getSecurityMarking());
        assertEquals("testSentence", statementArtifacts.getSentence());
        assertEquals("testStatementContentExtraValue", statementArtifacts.get("testStatementContentExtra").toString());

        ColumnFamily foundExtraColumnFamily = statement.get("testExtraColumnFamily");
        assertNotNull("foundExtraColumnFamily", foundExtraColumnFamily);
        assertEquals("testExtraValue", foundExtraColumnFamily.get("testExtraColumn").toString());
    }

    @Test
    public void testSave() {
        // ARRANGE

        StatementRowKey statementRowKey = new StatementRowKey(
                new TermRowKey("Bob", "CTA", "Person"),
                new PredicateRowKey("urn:mil.army.dsc:schema:dataspace", "knows"),
                new TermRowKey("Tina", "CTA", "Person")
        );
        Statement statement = new Statement(statementRowKey);
        StatementArtifact statementArtifact = new StatementArtifact()
            .setArtifactKey("testArtifactKey")
            .setAuthor("testAuthor")
            .setDate(100L)
            .setExtractorId("testExtractorId")
            .setSecurityMarking("testSecurityMarking")
            .setSentence("testSentence");

        statement.addColumnFamily(
                new ColumnFamily("testExtraColumnFamily")
                        .set("testExtraColumn", "testExtraValue"));

        statement.addStatementArtifact(statementArtifact);

        // ACT

        statementRepository.save(session, statement);

        // ASSERT

        assertEquals(1, session.tables.get(Statement.TABLE_NAME).size());
        Row statementRow = session.tables.get(Statement.TABLE_NAME).get(0);
        assertEquals("bob\u001FCTA\u001FPerson\u001Eurn:mil.army.dsc:schema:dataspace\u001Fknows\u001Etina\u001FCTA\u001FPerson", statementRow.getRowKey().toString());

        assertEquals(2, statementRow.getColumnFamilies().size());
        ColumnFamily firstColumnFamily = (ColumnFamily)statementRow.getColumnFamilies().iterator().next();
        assertEquals("urn\u001Fsha256\u001Feafce42c1f5203eb7007d7af91608dbc36b067446e2dd09cb7095e7e377d58ac", firstColumnFamily.getColumnFamilyName());
        assertEquals("testArtifactKey", firstColumnFamily.get(StatementArtifact.ARTIFACT_KEY).toString());
        assertEquals("testAuthor", firstColumnFamily.get(StatementArtifact.AUTHOR).toString());
        assertEquals((Long)100L, firstColumnFamily.get(StatementArtifact.DATE).toLong());
        assertEquals("testExtractorId", firstColumnFamily.get(StatementArtifact.EXTRACTOR_ID).toString());
        assertEquals("testSecurityMarking", firstColumnFamily.get(StatementArtifact.SECURITY_MARKING).toString());
        assertEquals("testSentence", firstColumnFamily.get(StatementArtifact.SENTENCE).toString());

        ColumnFamily extraColumnFamily = statementRow.get("testExtraColumnFamily");
        assertNotNull("extraColumnFamily", extraColumnFamily);
        assertEquals(1, extraColumnFamily.getColumns().size());
        assertEquals("testExtraValue", extraColumnFamily.get("testExtraColumn").toString());
    }
}
