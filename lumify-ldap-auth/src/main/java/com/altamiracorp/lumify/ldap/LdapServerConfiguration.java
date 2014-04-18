package com.altamiracorp.lumify.ldap;

import com.altamiracorp.lumify.core.config.Configurable;

public class LdapServerConfiguration {
    private String primaryLdapServerHostname;
    private int primaryLdapServerPort;
    private String failoverLdapServerHostname;
    private int failoverLdapServerPort;
    private int maxConnections;
    private String bindDn;
    private String bindPassword;
    private String trustStore;
    private String trustStorePassword;
    private String trustStoreType;

    @Configurable(name = "primary-server")
    public void setPrimaryLdapServerHostname(String primaryLdapServerHostname) {
        this.primaryLdapServerHostname = primaryLdapServerHostname;
    }

    @Configurable(name = "primary-port", defaultValue = "636")
    public void setPrimaryLdapServerPort(int primaryLdapServerPort) {
        this.primaryLdapServerPort = primaryLdapServerPort;
    }

    @Configurable(name = "failover-server", required = false)
    public void setFailoverLdapServerHostname(String failoverLdapServerHostname) {
        this.failoverLdapServerHostname = failoverLdapServerHostname;
    }

    @Configurable(name = "failover-port", defaultValue = "636")
    public void setFailoverLdapServerPort(int failoverLdapServerPort) {
        this.failoverLdapServerPort = failoverLdapServerPort;
    }

    @Configurable(name = "max-connections", defaultValue = "10")
    public void setMaxConnections(int maxConnections) {
        this.maxConnections = maxConnections;
    }

    @Configurable(name = "bind-dn")
    public void setBindDn(String bindDn) {
        this.bindDn = bindDn;
    }

    @Configurable(name = "bind-password")
    public void setBindPassword(String bindPassword) {
        this.bindPassword = bindPassword;
    }

    @Configurable(name = "trust-store")
    public void setTrustStore(String trustStore) {
        this.trustStore = trustStore;
    }

    @Configurable(name = "trust-store-password")
    public void setTrustStorePassword(String trustStorePassword) {
        this.trustStorePassword = trustStorePassword;
    }

    @Configurable(name = "trust-store-type", defaultValue = "JKS")
    public void setTrustStoreType(String trustStoreType) {
        this.trustStoreType = trustStoreType;
    }

    public String getPrimaryLdapServerHostname() {
        return primaryLdapServerHostname;
    }

    public int getPrimaryLdapServerPort() {
        return primaryLdapServerPort;
    }

    public String getFailoverLdapServerHostname() {
        return failoverLdapServerHostname;
    }

    public int getFailoverLdapServerPort() {
        return failoverLdapServerPort;
    }

    public int getMaxConnections() {
        return maxConnections;
    }

    public String getBindDn() {
        return bindDn;
    }

    public String getBindPassword() {
        return bindPassword;
    }

    public String getTrustStore() {
        return trustStore;
    }

    public String getTrustStorePassword() {
        return trustStorePassword;
    }

    public String getTrustStoreType() {
        return trustStoreType;
    }
}
