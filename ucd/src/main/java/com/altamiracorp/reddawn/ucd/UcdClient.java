package com.altamiracorp.reddawn.ucd;

import com.altamiracorp.reddawn.ucd.models.Artifact;
import com.altamiracorp.reddawn.ucd.models.ArtifactKey;
import com.altamiracorp.reddawn.ucd.models.Term;
import com.altamiracorp.reddawn.ucd.models.TermKey;
import org.apache.accumulo.core.client.*;
import org.apache.accumulo.core.data.Range;
import org.json.JSONException;

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
    initializeTable(Artifact.TABLE_NAME);
    initializeTable(Term.TABLE_NAME);
  }

  private void initializeTable(String tableName) throws TableExistsException, AccumuloSecurityException, AccumuloException {
    if (!connection.tableOperations().exists(tableName)) {
      connection.tableOperations().create(tableName);
    }
  }

  public void deleteTables() throws AccumuloSecurityException, AccumuloException, TableNotFoundException {
    deleteTable(Artifact.TABLE_NAME);
  }

  private void deleteTable(String tableName) throws AccumuloSecurityException, AccumuloException, TableNotFoundException {
    if (connection.tableOperations().exists(tableName)) {
      connection.tableOperations().delete(tableName);
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

  public void writeTerm(Term term, QueryUser<A> queryUser) throws UCDIOException {
    try {
      BatchWriter writer = this.connection.createBatchWriter(Term.TABLE_NAME, this.batchWriterMemBuf, this.batchWriterTimeout, this.batchWriterNumThreads);
      writer.addMutation(term.getMutation());
      writer.close();
    } catch (TableNotFoundException e) {
      throw new UCDIOException(e);
    } catch (MutationsRejectedException e) {
      throw new UCDIOException(e);
    }
  }

  public Term queryTermByKey(TermKey termKey, QueryUser<A> queryUser) throws UCDIOException {
    try {
      Scanner scan = this.connection.createScanner(Term.TABLE_NAME, queryUser.getAuthorizations());
      scan.setRange(new Range(termKey.toString()));

      List<Term> terms = Term.newBuilder().buildFromScanner(scan);
      if (terms.isEmpty()) {
        return null;
      }
      if (terms.size() != 1) {
        throw new UCDIOException("Multiple rows returned for term with key: " + termKey.toString());
      }
      return terms.get(0);
    } catch (TableNotFoundException e) {
      throw new UCDIOException(e);
    } catch (JSONException e) {
      throw new UCDIOException(e);
    }
  }
}
