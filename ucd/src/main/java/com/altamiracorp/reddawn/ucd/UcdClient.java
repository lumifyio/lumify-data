package com.altamiracorp.reddawn.ucd;

import com.altamiracorp.reddawn.ucd.models.Artifact;
import org.apache.accumulo.core.client.*;
import org.apache.accumulo.core.data.Range;
import org.apache.accumulo.core.security.Authorizations;

import java.util.List;

public class UcdClient {
  private final Connector accumuloConnector;

  public UcdClient(Connector accumuloConnector) {
    this.accumuloConnector = accumuloConnector;
  }

  public Connector getAccumuloConnector() {
    return accumuloConnector;
  }

  public void initializeTables() throws TableExistsException, AccumuloSecurityException, AccumuloException {
    if (!getAccumuloConnector().tableOperations().exists(Artifact.TABLE_NAME)) {
      getAccumuloConnector().tableOperations().create(Artifact.TABLE_NAME);
    }
  }

  public void deleteTables() throws AccumuloSecurityException, AccumuloException, TableNotFoundException {
    getAccumuloConnector().tableOperations().delete(Artifact.TABLE_NAME);
  }

  public Artifact getArtifactByRowId(String rowId, Authorizations auths) throws Exception {
    Scanner scan = getAccumuloConnector().createScanner(Artifact.TABLE_NAME, auths);
    scan.setRange(new Range(rowId));

    List<Artifact> artifacts = Artifact.createFromScanner(scan);
    if (artifacts.isEmpty()) {
      return null;
    }
    if (artifacts.size() != 1) {
      throw new Exception("Multiple rows returned for rowId: " + rowId); // TODO create custom exception for multiple rows
    }
    return artifacts.get(0);
  }

  public void close() {
  }
}
