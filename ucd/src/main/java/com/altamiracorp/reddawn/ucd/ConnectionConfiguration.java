package com.altamiracorp.reddawn.ucd;

public class ConnectionConfiguration {
  private String instanceName;
  private String zookeepers;
  private String username;
  private byte[] password;
  private int poolBatchThreadCount;

  public void setInstanceName(String instanceName) {
    this.instanceName = instanceName;
  }

  public String getInstanceName() {
    return instanceName;
  }

  public void setZookeepers(String zookeepers) {
    this.zookeepers = zookeepers;
  }

  public String getZookeepers() {
    return zookeepers;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public String getUsername() {
    return username;
  }

  public void setPassword(byte[] password) {
    this.password = password;
  }

  public byte[] getPassword() {
    return password;
  }

  public void setPoolBatchThreadCount(int poolBatchThreadCount) {
    this.poolBatchThreadCount = poolBatchThreadCount;
  }

  public int getPoolBatchThreadCount() {
    return poolBatchThreadCount;
  }
}
