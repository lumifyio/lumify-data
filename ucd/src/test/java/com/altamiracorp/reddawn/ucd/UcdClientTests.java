package com.altamiracorp.reddawn.ucd;

import com.altamiracorp.reddawn.ucd.models.Artifact;
import com.altamiracorp.reddawn.ucd.models.Term;
import com.altamiracorp.reddawn.ucd.models.TermMention;
import org.apache.accumulo.core.client.*;
import org.apache.accumulo.core.client.mock.MockConnector;
import org.apache.accumulo.core.client.mock.MockInstance;
import org.apache.accumulo.core.security.Authorizations;
import org.apache.accumulo.core.security.thrift.AuthInfo;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.collections.CollectionUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNotSame;

@RunWith(JUnit4.class)
public class UcdClientTests {
  private UcdClient client;

  @Before
  public void setup() throws AccumuloSecurityException, AccumuloException, TableExistsException {
    MockInstance mockInstance = new MockInstance();
    AuthInfo authInfo = new AuthInfo();
    authInfo.setUser("root");
    authInfo.setPassword(new byte[]{});
    MockConnector connection = (MockConnector) mockInstance.getConnector(authInfo);
    this.client = new UcdClient(connection);
    this.client.initializeTables();
  }

  @After
  public void tearDown() throws AccumuloSecurityException, AccumuloException, TableNotFoundException {
    this.client.deleteTables();
    this.client.close();
  }

  @Test
  public void testGetArtifactByRowId() throws Exception {
    Calendar cal = Calendar.getInstance();
    cal.set(2013, 4, 3, 11, 12, 0);

    long memBuf = 1000000L; // bytes to store before sending a batch
    long timeout = 1000L; // milliseconds to wait before sending
    int numThreads = 10;

    // write artifact
    BatchWriter writer = this.client.getAccumuloConnector().createBatchWriter(Artifact.TABLE_NAME, memBuf, timeout, numThreads);
    Artifact artifact = new Artifact();
    artifact.setFullFileName("testGetArtifactByRowId.txt");
    artifact.setData("testGetArtifactByRowId");
    artifact.setExtractedText("testGetArtifactByRowIdText");
    artifact.setDocumentDateTime(cal.getTime());
    artifact.setSubject("subject");
    writer.addMutation(artifact.getMutation());
    writer.close();

    // read artifact
    Authorizations auth = new Authorizations();
    Artifact foundArtifact = this.client.getArtifactByRowId(artifact.getRowId(), auth);
    assertNotNull("Could not find artifact with id: " + artifact.getRowId(), foundArtifact);
    assertEquals(artifact.getRowId(), foundArtifact.getRowId());

    assertEquals(new String(Hex.encodeHex(artifact.getData())), new String(Hex.encodeHex(foundArtifact.getData())));
    assertEquals(artifact.getExtractedTextAsString(), foundArtifact.getExtractedTextAsString());

    assertEquals(artifact.getFileExtension(), foundArtifact.getFileExtension());
    assertEquals(artifact.getFileName(), foundArtifact.getFileName());
    assertEquals(artifact.getDocumentDateTime().toString(), foundArtifact.getDocumentDateTime().toString());
    assertEquals(artifact.getSubject(), foundArtifact.getSubject());
  }

  @Test
  public void testGetTerms() throws Exception {
    long memBuf = 1000000L; // bytes to store before sending a batch
    long timeout = 1000L; // milliseconds to wait before sending
    int numThreads = 10;

    // write artifact
    BatchWriter writer = this.client.getAccumuloConnector().createBatchWriter(Term.TABLE_NAME, memBuf, timeout, numThreads);
    Term term = new Term();
    term.setSign("joe");
    term.setModelKey("reddawn");
    term.setConceptLabel("PERSON");

    TermMention termMention = new TermMention();
    termMention.setArtifactKey("urn:sha256:123456");
    termMention.setMention("{\"start\":1234,\"end\":2345}");
    term.addTermMention(termMention);

    writer.addMutation(term.getMutation());
    writer.close();

    // read artifact
    Authorizations auth = new Authorizations();
    List<Term> foundTerms = this.client.getTermsStartingWith("joe", auth);
    assertNotNull("Could not find term starting with: joe", foundTerms);

    assertEquals(1, foundTerms.size());
    Term foundTerm = foundTerms.get(0);
    assertEquals(term.getRowId(), foundTerm.getRowId());
    assertEquals(term.getSign(), foundTerm.getSign());
    assertEquals(term.getModelKey(), foundTerm.getModelKey());
    assertEquals(term.getConceptLabel(), foundTerm.getConceptLabel());

    ArrayList<TermMention> foundTermMentions = new ArrayList<TermMention>();
    CollectionUtils.addAll(foundTermMentions, foundTerm.getTermMentions().iterator());
    assertEquals(1, foundTermMentions.size());

    TermMention foundTermMention = foundTermMentions.get(0);
    assertEquals(termMention.getRowId(), foundTermMention.getRowId());
    assertEquals(termMention.getArtifactKey(), foundTermMention.getArtifactKey());
    assertEquals(termMention.getMention(), foundTermMention.getMention());
  }
}
