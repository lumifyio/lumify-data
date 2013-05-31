package com.altamiracorp.reddawn.ucd;

import com.altamiracorp.reddawn.ucd.model.*;
import com.altamiracorp.reddawn.ucd.model.artifact.ArtifactKey;
import com.altamiracorp.reddawn.ucd.model.terms.TermKey;
import com.altamiracorp.reddawn.ucd.model.terms.TermMention;
import com.altamiracorp.reddawn.ucd.model.terms.TermMetadata;
import org.apache.accumulo.core.client.AccumuloException;
import org.apache.accumulo.core.client.AccumuloSecurityException;
import org.apache.accumulo.core.client.TableExistsException;
import org.apache.accumulo.core.client.TableNotFoundException;
import org.apache.accumulo.core.client.mock.MockConnector;
import org.apache.accumulo.core.client.mock.MockInstance;
import org.apache.accumulo.core.security.thrift.AuthInfo;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

@RunWith(JUnit4.class)
public class UcdClientTest {
  private UcdClient<AuthorizationLabel> client;

  @Before
  public void setup() throws AccumuloSecurityException, AccumuloException, TableExistsException {
    MockInstance mockInstance = new MockInstance();
    AuthInfo authInfo = new AuthInfo();
    authInfo.setUser("root");
    authInfo.setPassword(new byte[]{});
    MockConnector connection = (MockConnector) mockInstance.getConnector(authInfo);
    this.client = new MockUcdClient<AuthorizationLabel>(connection);
    this.client.initializeTables();
  }

  @After
  public void tearDown() throws AccumuloSecurityException, AccumuloException, TableNotFoundException {
    this.client.deleteTables();
    this.client.close();
  }

  @Test
  public void testWriteArtifact() throws UCDIOException, InvalidClassificationException {
    ArtifactContent artifactContent = ArtifactContent.newBuilder()
        .docArtifactBytes("testArtifactBytes".getBytes())
        .docExtractedText("testArtifactExtractedText")
        .security("testSecurity")
        .build();
    ArtifactGenericMetadata artifactGenericMetadata = ArtifactGenericMetadata.newBuilder()
        .author("testAuthor")
        .category("testCategory")
        .charset("testCharset")
        .documentDtg("testDocumentDtg")
        .documentType("testDocumentType")
        .externalUrl("testExternalUrl")
        .extractedTextHdfsPath("testExtractedTextHdfsPath")
        .fileExtension("testFileExtension")
        .fileName("testFileName")
        .fileTimestamp(1L)
        .hdfsFilePath("testHdfsFilePath")
        .language("testLanguage")
        .loadTimestamp(2L)
        .fileSize(3L)
        .loadType("testLoadType")
        .mimeType("testMimeTest")
        .source("testSource")
        .sourceSubType("testSourceSubType")
        .sourceType("testSourceType")
        .subject("testSubject")
        .build();
    ArtifactDynamicMetadata artifactDynamicMetadata = ArtifactDynamicMetadata.newBuilder()
        .artifactSerialNum("testartifactSerialNum")
        .docSourceHash("testDocSourceHash")
        .edhGuid("testEdhGuid")
        .geoLocation("testGeoLocation")
        .provenanceId("testProvenanceId")
        .sourceHashAlgorithm("testSourceHashAlgorithm")
        .sourceLabel("testSourceLabel")
        .build();
    Artifact artifact = Artifact.newBuilder()
        .artifactContent(artifactContent)
        .artifactGenericMetadata(artifactGenericMetadata)
        .artifactDynamicMetadata(artifactDynamicMetadata)
        .build();
    QueryUser<AuthorizationLabel> queryUser = new QueryUser<AuthorizationLabel>("root", new SimpleAuthorizationLabel("U"));
    this.client.writeArtifact(artifact, queryUser);

    Artifact foundArtifact = this.client.queryArtifactByKey(artifact.getKey(), queryUser);
    assertNotNull("artifact with key " + artifact.getKey() + " was null", foundArtifact);
    assertEquals("urn\u001Fsha256\u001Ffd3d29f773fba0fb911bc0b7012d8a103cf14fa541eb3a66450a771421bcbe61", foundArtifact.getKey().toString());

    // content fields
    assertEquals("testArtifactBytes", new String(foundArtifact.getContent().getDocArtifactBytes()));
    assertEquals("testArtifactExtractedText", foundArtifact.getContent().getDocExtractedText());
    assertEquals("testSecurity", foundArtifact.getContent().getSecurity());

    // generic metadata
    assertEquals("testAuthor", foundArtifact.getGenericMetadata().getAuthor());
    assertEquals("testCategory", foundArtifact.getGenericMetadata().getCategory());
    assertEquals("testCharset", foundArtifact.getGenericMetadata().getCharset());
    assertEquals("testDocumentDtg", foundArtifact.getGenericMetadata().getDocumentDtg());
    assertEquals("testDocumentType", foundArtifact.getGenericMetadata().getDocumentType());
    assertEquals("testExternalUrl", foundArtifact.getGenericMetadata().getExternalUrl());
    assertEquals("testExtractedTextHdfsPath", foundArtifact.getGenericMetadata().getExtractedTextHdfsPath());
    assertEquals("testFileExtension", foundArtifact.getGenericMetadata().getFileExtension());
    assertEquals("testFileName", foundArtifact.getGenericMetadata().getFileName());
    assertEquals(1L, foundArtifact.getGenericMetadata().getFileTimestamp().longValue());
    assertEquals("testHdfsFilePath", foundArtifact.getGenericMetadata().getHdfsFilePath());
    assertEquals("testLanguage", foundArtifact.getGenericMetadata().getLanguage());
    assertEquals(2L, foundArtifact.getGenericMetadata().getLoadTimestamp().longValue());
    assertEquals(3L, foundArtifact.getGenericMetadata().getFileSize().longValue());
    assertEquals("testLoadType", foundArtifact.getGenericMetadata().getLoadType());
    assertEquals("testMimeTest", foundArtifact.getGenericMetadata().getMimeType());
    assertEquals("testSource", foundArtifact.getGenericMetadata().getSource());
    assertEquals("testSourceSubType", foundArtifact.getGenericMetadata().getSourceSubType());
    assertEquals("testSourceType", foundArtifact.getGenericMetadata().getSourceType());
    assertEquals("testSubject", foundArtifact.getGenericMetadata().getSubject());

    // dynamic metadata
    assertEquals("testartifactSerialNum", foundArtifact.getDynamicMetadata().getArtifactSerialNum());
    assertEquals("testDocSourceHash", foundArtifact.getDynamicMetadata().getDocSourceHash());
    assertEquals("testEdhGuid", foundArtifact.getDynamicMetadata().getEdhGuid());
    assertEquals("testGeoLocation", foundArtifact.getDynamicMetadata().getGeoLocation());
    assertEquals("testProvenanceId", foundArtifact.getDynamicMetadata().getProvenanceId());
    assertEquals("testSourceHashAlgorithm", foundArtifact.getDynamicMetadata().getSourceHashAlgorithm());
    assertEquals("testSourceLabel", foundArtifact.getDynamicMetadata().getSourceLabel());
  }

  @Test
  public void testWriteTerm() throws UCDIOException, InvalidClassificationException {
    TermKey termKey = TermKey.newBuilder()
        .sign("testSign")
        .concept("testConcept")
        .model("UCD")
        .build();

    TermMetadata termMetadata1 = TermMetadata.newBuilder()
        .artifactKey(new ArtifactKey("testArtifactKey1"))
        .artifactKeySign("testArtifactKeySign1")
        .author("testAuthor1")
        .mention(new TermMention(0, 5))
        .build();

    TermMetadata termMetadata2 = TermMetadata.newBuilder()
        .artifactKey(new ArtifactKey("testArtifactKey2"))
        .artifactKeySign("testArtifactKeySign2")
        .author("testAuthor2")
        .mention(new TermMention(10, 15))
        .build();

    Term term = Term.newBuilder()
        .key(termKey)
        .metadata(termMetadata1)
        .metadata(termMetadata2)
        .build();

    QueryUser<AuthorizationLabel> queryUser = new QueryUser<AuthorizationLabel>("root", new SimpleAuthorizationLabel("U"));
    this.client.writeTerm(term, queryUser);

    Term foundTerm = this.client.queryTermByKey(term.getKey(), queryUser);
    assertNotNull("term with key " + term.getKey() + " was null", foundTerm);
    assertEquals("testsign\u001FUCD\u001FtestConcept", foundTerm.getKey().toString());
    assertEquals("testsign", foundTerm.getKey().getSign());
    assertEquals("UCD", foundTerm.getKey().getModel());
    assertEquals("testConcept", foundTerm.getKey().getConcept());

    ArrayList<TermMetadata> termMetadatas = new ArrayList<TermMetadata>(foundTerm.getMetadata());
    Collections.sort(termMetadatas);

    TermMetadata foundTermMetadata1 = termMetadatas.get(0);
    assertEquals("testArtifactKey1", foundTermMetadata1.getArtifactKey().toString());
    assertEquals("testArtifactKeySign1", foundTermMetadata1.getArtifactKeySign());
    assertEquals("testAuthor1", foundTermMetadata1.getAuthor());
    assertEquals("{\"start\":0,\"end\":5}", foundTermMetadata1.getMention().toString());

    ArtifactTermIndex artifactTermIndex1 = this.client.queryArtifactTermIndexByArtifactKey(foundTermMetadata1.getArtifactKey(), queryUser);
    assertNotNull("Could not find ArtifactTermIndex with ArtifactKey: " + foundTermMetadata1.getArtifactKey().toString(), artifactTermIndex1);
    assertEquals("testArtifactKey1", artifactTermIndex1.getKey().toString());
    Map<TermKey, List<String>> termMentions1 = artifactTermIndex1.getTermMentions();
    assertEquals(1, termMentions1.keySet().size());
    assertTrue("Could not find term", termMentions1.containsKey(new TermKey("testSign\u001FUCD\u001FtestConcept")));
    List<String> termMention1 = termMentions1.get(new TermKey("testSign\u001FUCD\u001FtestConcept"));
    assertEquals(1, termMention1.size());
    assertEquals("urn\u001Fsha256\u001F1bceee398439c4c8d72450b29a6ae55911e326757187ccc6fb822425a2358dee", termMention1.get(0));

    TermMetadata foundTermMetadata2 = termMetadatas.get(1);
    assertEquals("testArtifactKey2", foundTermMetadata2.getArtifactKey().toString());
    assertEquals("testArtifactKeySign2", foundTermMetadata2.getArtifactKeySign());
    assertEquals("testAuthor2", foundTermMetadata2.getAuthor());
    assertEquals("{\"start\":10,\"end\":15}", foundTermMetadata2.getMention().toString());

    ArtifactTermIndex artifactTermIndex2 = this.client.queryArtifactTermIndexByArtifactKey(foundTermMetadata2.getArtifactKey(), queryUser);
    assertEquals("testArtifactKey2", artifactTermIndex2.getKey().toString());
    Map<TermKey, List<String>> termMentions2 = artifactTermIndex2.getTermMentions();
    assertEquals(1, termMentions2.keySet().size());
    assertTrue("Could not find term", termMentions2.containsKey(new TermKey("testSign\u001FUCD\u001FtestConcept")));
    List<String> termMention2 = termMentions2.get(new TermKey("testSign\u001FUCD\u001FtestConcept"));
    assertEquals(1, termMention2.size());
    assertEquals("urn\u001Fsha256\u001Fecd6930868bc1f4f430b53cf504b9a69ee26150f3e7371975378847190fc3127", termMention2.get(0));
  }

  @Test
  public void testWriteArtifactTermIndex() throws UCDIOException, InvalidClassificationException {
    ArtifactTermIndex.Builder a = ArtifactTermIndex.newBuilder();

    ArtifactKey artifactKey = new ArtifactKey("artifact1");

    ArtifactTermIndex artifactTermIndex = a
        .artifactKey(artifactKey)
        .termMention(new TermKey("termRow1\u001Ftest\u001Fperson"), "termMention1")
        .termMention(new TermKey("termRow1\u001Ftest\u001Fperson"), "termMention2")
        .build();

    QueryUser<AuthorizationLabel> queryUser = new QueryUser<AuthorizationLabel>("root", new SimpleAuthorizationLabel("U"));
    this.client.writeArtifactTermIndex(artifactTermIndex, queryUser);

    ArtifactTermIndex foundArtifactTermIndex = this.client.queryArtifactTermIndexByArtifactKey(artifactKey, queryUser);

    assertEquals("artifact1", foundArtifactTermIndex.getKey().toString());

    Map<TermKey, List<String>> termMentions = foundArtifactTermIndex.getTermMentions();
    assertEquals(1, termMentions.keySet().size());
    assertTrue("'termRow1' not found", termMentions.containsKey(new TermKey("termRow1\u001Ftest\u001Fperson")));

    List<String> mentions = termMentions.get(new TermKey("termRow1\u001Ftest\u001Fperson"));
    Collections.sort(mentions);

    assertEquals(2, mentions.size());
    assertEquals("termMention1", mentions.get(0));
    assertEquals("termMention2", mentions.get(1));
  }
}
