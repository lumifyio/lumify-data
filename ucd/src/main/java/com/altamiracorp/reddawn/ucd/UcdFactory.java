package com.altamiracorp.reddawn.ucd;

import java.util.Properties;

public class UcdFactory {
  /**
   * ConnectionConfiguration config = new ConnectionConfiguration();
   * config.setInstanceName("gm");
   * config.setZookeepers("10.10.31.152:2181");
   * config.setUsername("root");
   * config.setPassword("toor");
   * config.setPoolBatchThreadCount(1);
   *
   * Properties properties = new Properties();
   * properties.setProperty(UcdPropertyNames.BASE_ATTACHMENTS_URL, "https://jboss.fester.teaminvertix.int/content.war");
   * UcdClient<AuthorizationLabel> facade = UCDFactory.createUcdClient(config,properties);
   */
  public static UcdClient<AuthorizationLabel> createUcdClient(ConnectionConfiguration config, Properties properties) {
    return null;
  }
}
