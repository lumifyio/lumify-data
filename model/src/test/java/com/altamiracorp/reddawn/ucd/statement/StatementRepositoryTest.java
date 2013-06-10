package com.altamiracorp.reddawn.ucd.statement;

import com.altamiracorp.reddawn.model.MockSession;
import com.altamiracorp.reddawn.ucd.sentence.SentenceRepository;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

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
  public void testFindByRowKey() {
    assertEquals(1, 1); // todo: implement test
  }
}
