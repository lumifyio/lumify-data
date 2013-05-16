package com.altamiracorp.reddawn.ucd;

import org.apache.accumulo.core.client.AccumuloException;
import org.apache.accumulo.core.client.AccumuloSecurityException;
import org.apache.accumulo.core.client.Connector;
import org.apache.accumulo.core.client.ZooKeeperInstance;
import org.apache.accumulo.core.security.Authorizations;

import java.util.Properties;

public class UcdFactory {
  /**
   * ConnectionConfiguration config = new ConnectionConfiguration();
   * config.setInstanceName("gm");
   * config.setZookeepers("10.10.31.152:2181");
   * config.setUsername("root");
   * config.setPassword("toor");
   * config.setPoolBatchThreadCount(1);
   * <p/>
   * Properties properties = new Properties();
   * properties.setProperty(UcdPropertyNames.BASE_ATTACHMENTS_URL, "https://jboss.fester.teaminvertix.int/content.war");
   * UcdClient<AuthorizationLabel> facade = UCDFactory.createUcdClient(config,properties);
   */
  public static UcdClient<AuthorizationLabel> createUcdClient(ConnectionConfiguration config, Properties properties) throws AccumuloSecurityException, AccumuloException {
    ZooKeeperInstance zooKeeperInstance = new ZooKeeperInstance(config.getInstanceName(), config.getZookeepers());
    Connector connection = zooKeeperInstance.getConnector(config.getUsername(), config.getPassword());
    return new UcdClient<AuthorizationLabel>(connection);
  }
}
