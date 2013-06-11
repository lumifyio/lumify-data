package com.altamiracorp.reddawn.ucd.statement;

import com.altamiracorp.reddawn.model.ColumnFamily;
import com.altamiracorp.reddawn.model.MockSession;
import com.altamiracorp.reddawn.model.Row;
import com.altamiracorp.reddawn.model.RowKey;
import com.altamiracorp.reddawn.ucd.predicate.PredicateRowKey;
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
        // SETUP
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

        // DO IT

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
}
