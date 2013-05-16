package com.altamiracorp.reddawn.ucd;

import com.altamiracorp.reddawn.ucd.models.Artifact;
import com.altamiracorp.reddawn.ucd.models.ArtifactContent;
import com.altamiracorp.reddawn.ucd.models.ArtifactDynamicMetadata;
import com.altamiracorp.reddawn.ucd.models.ArtifactGenericMetadata;
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(JUnit4.class)
public class UcdClientTests {
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
}
