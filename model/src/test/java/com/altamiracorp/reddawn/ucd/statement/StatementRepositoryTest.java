package com.altamiracorp.reddawn.ucd.statement;

import com.altamiracorp.reddawn.model.MockSession;
import com.altamiracorp.reddawn.ucd.predicate.PredicateRowKey;
import com.altamiracorp.reddawn.ucd.term.TermRowKey;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
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
}
