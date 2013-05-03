package com.altamiracorp.redDawn.ucd;

import com.altamiracorp.redDawn.ucd.UcdClient;
import com.altamiracorp.redDawn.ucd.models.Artifact;
import org.apache.accumulo.core.client.*;
import org.apache.accumulo.core.security.Authorizations;
import org.apache.commons.codec.binary.Hex;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.Calendar;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;

@RunWith(JUnit4.class)
public class UcdClientTests {
  private UcdClient client;

  @Before
  public void setup() throws AccumuloSecurityException, AccumuloException, TableExistsException {
    String instanceName = "reddawn";
    String zooServers = "192.168.211.130";
    ZooKeeperInstance zooKeeperInstance = new ZooKeeperInstance(instanceName, zooServers);
    Connector connection = zooKeeperInstance.getConnector("root", "reddawn");
    this.client = new UcdClient(connection);
    this.client.initializeTables();
  }

  @After
  public void tearDown() throws AccumuloSecurityException, AccumuloException, TableNotFoundException {
    this.client.deleteTables();
    this.client.close();
  }

  @Test
  public void testWriteThenRead() throws Exception {
    Calendar cal = Calendar.getInstance();
    cal.set(2013, 4, 3, 11, 12, 0);

    long memBuf = 1000000L; // bytes to store before sending a batch
    long timeout = 1000L; // milliseconds to wait before sending
    int numThreads = 10;

    // write artifact
    BatchWriter writer = this.client.getAccumuloConnector().createBatchWriter(Artifact.TABLE_NAME, memBuf, timeout, numThreads);
    Artifact artifact = new Artifact();
    artifact.setFullFileName("testWriteThenRead.txt");
    artifact.setData("testGetUpdates");
    artifact.setExtractedText("testGetUpdatesText");
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
}
