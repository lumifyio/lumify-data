package com.altamiracorp.reddawn.ucd;

import com.altamiracorp.reddawn.ucd.models.Artifact;
import com.altamiracorp.reddawn.ucd.models.ArtifactKey;
import org.apache.accumulo.core.client.*;
import org.apache.accumulo.core.data.Range;

import java.util.List;

public class UcdClient<A extends AuthorizationLabel> {
  protected final Connector connection;
  private long batchWriterMemBuf = 1000000L;
  private long batchWriterTimeout = 1000L;
  private int batchWriterNumThreads = 10;

  UcdClient(Connector connection) {
    this.connection = connection;
  }

  public void initializeTables() throws TableExistsException, AccumuloSecurityException, AccumuloException {
    if (!connection.tableOperations().exists(Artifact.TABLE_NAME)) {
      connection.tableOperations().create(Artifact.TABLE_NAME);
    }
  }

  public void deleteTables() throws AccumuloSecurityException, AccumuloException, TableNotFoundException {
    if (connection.tableOperations().exists(Artifact.TABLE_NAME)) {
      connection.tableOperations().delete(Artifact.TABLE_NAME);
    }
  }

  public void close() {

  }

  public void writeArtifact(Artifact artifact, QueryUser<A> queryUser) throws UCDIOException, InvalidClassificationException {
    try {
      BatchWriter writer = this.connection.createBatchWriter(Artifact.TABLE_NAME, this.batchWriterMemBuf, this.batchWriterTimeout, this.batchWriterNumThreads);
      writer.addMutation(artifact.getMutation());
      writer.close();
    } catch (TableNotFoundException e) {
      throw new UCDIOException(e);
    } catch (MutationsRejectedException e) {
      throw new UCDIOException(e);
    }
  }

  public Artifact queryArtifactByKey(ArtifactKey artifactKey, QueryUser<A> queryUser) throws UCDIOException {
    try {
      Scanner scan = this.connection.createScanner(Artifact.TABLE_NAME, queryUser.getAuthorizations());
      scan.setRange(new Range(artifactKey.toString()));

      List<Artifact> artifacts = Artifact.newBuilder().buildFromScanner(scan);
      if (artifacts.isEmpty()) {
        return null;
      }
      if (artifacts.size() != 1) {
        throw new UCDIOException("Multiple rows returned for artifact with key: " + artifactKey.toString());
      }
      return artifacts.get(0);
    } catch (TableNotFoundException e) {
      throw new UCDIOException(e);
    }
  }

  public Connector getConnection() {
    return connection;
  }
}
