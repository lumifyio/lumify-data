package com.altamiracorp.lumify.config;

/**
 * Constants for web application configuration parameters
 */
public final class ConfigConstants {

    public static final String APP_CONFIG_LOCATION = "application.config.location";
    public static final String APP_CREDENTIALS_LOCATION = "application.config.credentials.location";

    public static final String HADOOP_URL = "hadoop.url";
    public static final String ZK_INSTANCENAME = "zookeeper.instanceName";
    public static final String ZK_SERVERS = "zookeeper.serverNames";
    public static final String ACCUMULO_USER = "accumulo.username";
    public static final String ACCUMULO_PASSWORD = "accumulo.password";
    public static final String BLUR_CONTROLLER = "blur.controllerLocation";
    public static final String BLUR_PATH = "blur.path";
    public static final String GRAPH_SEARCH_HOSTNAME = "graph.search.hostname";
    public static final String MAP_PROVIDER = "map.provider";
    public static final String MAP_ACCESS_KEY = "map.apiKey";
    public static final String MAP_TILE_SERVER_HOST = "map.tileServer.hostName";
    public static final String MAP_TILE_SERVER_PORT = "map.tileServer.port";
    public static final String SEARCH_PROVIDER = "search.provider";
    public static final String ELASTIC_SEARCH_LOCATIONS = "elasticsearch.locations";
    public static final String AUTHENTICATION_PROVIDER = "authentication.provider";

    private ConfigConstants() {

    }
}
