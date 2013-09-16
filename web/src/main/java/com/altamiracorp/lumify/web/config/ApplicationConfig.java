package com.altamiracorp.lumify.web.config;

/**
 * Interface for accessing web application specific configuration values
 */
public interface ApplicationConfig {
    public String getNamenodeUrl();
    public String getZookeeperInstanceName();
    public String getZookeeperServerNames();
    public String getDataStoreUserName();
    public String getDataStorePassword();
    public String getSearchIndexController();
    public String getSearchIndexStoragePath();
    public String getGraphSearchIndexHostname();
    public String getSearchProvider();
    public String getElasticSearchLocations();
    public String getAuthenticationProvider();
}
