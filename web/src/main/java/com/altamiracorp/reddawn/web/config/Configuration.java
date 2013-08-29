package com.altamiracorp.reddawn.web.config;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Responsible for parsing application configuration file and providing
 * configuration values to the application
 */
public final class Configuration implements MapConfig, ApplicationConfig {
    private static final Logger LOGGER = LoggerFactory.getLogger(Configuration.class);

    /**
     * Default value for a {@link String} property that could not be parsed
     */
    private static final String UNKNOWN_STRING = "Unknown";

    /**
     * Default value for a {@link Integer} property that could not be parsed
     */
    private static final String UNKNOWN_INT = "-999";

    private static String nameNodeUrl;
    private static String zkInstanceName;
    private static String zkServerNames;
    private static String dataStoreUserName;
    private static String dataStorePassword;
    private static String searchIndexController;
    private static String searchIndexStoragePath;
    private static String graphSearchIndexHostname;
    private static String mapProvider;
    private static String mapAccessKey;
    private static String mapTileServerHostname;
    private static int mapTileServerPort;

    private Configuration() {

    }

    @Override
    public String getNamenodeUrl() {
        return nameNodeUrl;
    }

    @Override
    public String getZookeeperInstanceName() {
        return zkInstanceName;
    }

    @Override
    public String getZookeeperServerNames() {
        return zkServerNames;
    }

    @Override
    public String getDataStoreUserName() {
        return dataStoreUserName;
    }

    @Override
    public String getDataStorePassword() {
        return dataStorePassword;
    }

    @Override
    public String getSearchIndexController() {
        return searchIndexController;
    }

    @Override
    public String getSearchIndexStoragePath() {
        return searchIndexStoragePath;
    }

    @Override
    public String getGraphSearchIndexHostname() {
        return graphSearchIndexHostname;
    }

    @Override
    public String getMapProvider() {
        return mapProvider;
    }

    @Override
    public String getMapAccessKey() {
        return mapAccessKey;
    }

    @Override
    public String getMapTileServerHostname() {
        return mapTileServerHostname;
    }

    @Override
    public int getMapTileServerPort() {
        return mapTileServerPort;
    }

    /**
     * Attempts to parse the application configuration file located at the
     * specified filesystem path
     * @param fileUrl The URL to the configuration file, not null or empty
     * @return A {@link Configuration} object that contains the parsed configuration values
     */
    public static Configuration loadConfigurationFile(final String fileUrl) {
        checkNotNull(fileUrl, "The specified file URL was null");
        checkArgument(!fileUrl.isEmpty(), "The specified file URL was empty");

        final Properties prop = new Properties();

        try {
            URL url = new URL(fileUrl);
            prop.load(url.openStream());
        } catch (FileNotFoundException e) {
            LOGGER.error("Could not find file to load at: " + fileUrl, e);
            throw new RuntimeException(e);
        } catch (IOException e) {
            LOGGER.error("Error occurred while loading file", e);
            throw new RuntimeException(e);
        }

        // Extract the expected configuration properties
        nameNodeUrl = prop.getProperty(WebConfigConstants.HADOOP_URL, UNKNOWN_STRING);
        zkInstanceName = prop.getProperty(WebConfigConstants.ZK_INSTANCENAME, UNKNOWN_STRING);
        zkServerNames = prop.getProperty(WebConfigConstants.ZK_SERVERS, UNKNOWN_STRING);
        dataStoreUserName = prop.getProperty(WebConfigConstants.ACCUMULO_USER, UNKNOWN_STRING);
        dataStorePassword = prop.getProperty(WebConfigConstants.ACCUMULO_PASSWORD, UNKNOWN_STRING);
        searchIndexController = prop.getProperty(WebConfigConstants.BLUR_CONTROLLER, UNKNOWN_STRING);
        searchIndexStoragePath = prop.getProperty(WebConfigConstants.BLUR_PATH, UNKNOWN_STRING);
        graphSearchIndexHostname = prop.getProperty(WebConfigConstants.GRAPH_SEARCH_HOSTNAME, UNKNOWN_STRING);
        mapProvider = prop.getProperty(WebConfigConstants.MAP_PROVIDER, UNKNOWN_STRING);
        mapAccessKey = prop.getProperty(WebConfigConstants.MAP_ACCESS_KEY, UNKNOWN_STRING);
        mapTileServerHostname = prop.getProperty(WebConfigConstants.MAP_TILE_SERVER_HOST, UNKNOWN_STRING);
        mapTileServerPort = Integer.parseInt(prop.getProperty(WebConfigConstants.MAP_TILE_SERVER_PORT, UNKNOWN_INT));

        return new Configuration();
    }
}
