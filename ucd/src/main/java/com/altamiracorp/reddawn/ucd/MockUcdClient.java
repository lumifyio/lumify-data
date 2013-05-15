package com.altamiracorp.reddawn.ucd;

import org.apache.accumulo.core.client.mock.MockConnector;

public class MockUcdClient<A extends AuthorizationLabel> extends UcdClient<A> {
  public MockUcdClient(MockConnector connection) {
    super(connection);
  }

  public MockConnector getConnection() {
    return (MockConnector) this.connection;
  }
}
