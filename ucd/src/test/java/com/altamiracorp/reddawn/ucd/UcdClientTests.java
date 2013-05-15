package com.altamiracorp.reddawn.ucd;

import com.altamiracorp.reddawn.ucd.models.Artifact;
import com.altamiracorp.reddawn.ucd.models.ArtifactContent;
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
        .build();
    Artifact artifact = Artifact.newBuilder()
        .artifactContent(artifactContent)
        .build();
    QueryUser<AuthorizationLabel> queryUser = new QueryUser<AuthorizationLabel>("root", new SimpleAuthorizationLabel("U"));
    this.client.writeArtifact(artifact, queryUser);

    Artifact foundArtifact = this.client.queryArtifactByKey(artifact.getKey(), queryUser);
    assertNotNull("artifact with key " + artifact.getKey() + " was null", foundArtifact);
    assertEquals("urn\u001Fsha256\u001Ffd3d29f773fba0fb911bc0b7012d8a103cf14fa541eb3a66450a771421bcbe61", foundArtifact.getKey().toString());
    assertEquals("testArtifactBytes", new String(foundArtifact.getContent().getDocArtifactBytes()));
    // TODO test all columns
  }
}
