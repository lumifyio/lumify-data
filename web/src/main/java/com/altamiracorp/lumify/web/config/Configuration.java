package com.altamiracorp.lumify.web.config;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Objects;

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
    private static String searchProvider;
    private static String elasticSearchLocations;
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
    public String getSearchProvider() {
        return searchProvider;
    }

    @Override
    public String getElasticSearchLocations() {
        return elasticSearchLocations;
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
     * @param configUrl The URL to the configuration file, not null or empty
     * @param credentialsUrl The URL to the credentials file, not null or empty
     * @return A {@link Configuration} object that contains the parsed configuration values
     */
    public static Configuration loadConfigurationFile(final String configUrl, final String credentialsUrl) {
        checkNotNull(configUrl, "The specified config file URL was null");
        checkArgument(!configUrl.isEmpty(), "The specified config file URL was empty");
        checkNotNull(credentialsUrl, "The specified credentials URL was null");
        checkArgument(!credentialsUrl.isEmpty(), "The specified credentials URL was empty");

        LOGGER.debug(String.format("Attemping to load configuration file: %s and credentials file: %s", configUrl, credentialsUrl));

        final Properties mergedProps = new Properties();

        processFile(configUrl, mergedProps);
        processFile(credentialsUrl, mergedProps);

        // Extract the expected configuration properties
        nameNodeUrl = mergedProps.getProperty(WebConfigConstants.HADOOP_URL, UNKNOWN_STRING);
        zkInstanceName = mergedProps.getProperty(WebConfigConstants.ZK_INSTANCENAME, UNKNOWN_STRING);
        zkServerNames = mergedProps.getProperty(WebConfigConstants.ZK_SERVERS, UNKNOWN_STRING);
        dataStoreUserName = mergedProps.getProperty(WebConfigConstants.ACCUMULO_USER, UNKNOWN_STRING);
        dataStorePassword = mergedProps.getProperty(WebConfigConstants.ACCUMULO_PASSWORD, UNKNOWN_STRING);
        searchIndexController = mergedProps.getProperty(WebConfigConstants.BLUR_CONTROLLER, UNKNOWN_STRING);
        searchIndexStoragePath = mergedProps.getProperty(WebConfigConstants.BLUR_PATH, UNKNOWN_STRING);
        graphSearchIndexHostname = mergedProps.getProperty(WebConfigConstants.GRAPH_SEARCH_HOSTNAME, UNKNOWN_STRING);
        mapProvider = mergedProps.getProperty(WebConfigConstants.MAP_PROVIDER, UNKNOWN_STRING);
        mapAccessKey = mergedProps.getProperty(WebConfigConstants.MAP_ACCESS_KEY, UNKNOWN_STRING);
        mapTileServerHostname = mergedProps.getProperty(WebConfigConstants.MAP_TILE_SERVER_HOST, UNKNOWN_STRING);
        mapTileServerPort = Integer.parseInt(mergedProps.getProperty(WebConfigConstants.MAP_TILE_SERVER_PORT, UNKNOWN_INT));
        searchProvider = mergedProps.getProperty(WebConfigConstants.SEARCH_PROVIDER);
        elasticSearchLocations = mergedProps.getProperty(WebConfigConstants.ELASTIC_SEARCH_LOCATIONS);

        return new Configuration();
    }

    private static void processFile(final String fileUrl, final Properties props) {
        try {
            final URL url = new URL(fileUrl);
            props.load(url.openStream());
        } catch (MalformedURLException e) {
            LOGGER.error("Could not create URL object for malformed URL: " + fileUrl, e);
            throw new RuntimeException(e);
        } catch (FileNotFoundException e) {
            LOGGER.info("Could not find file to load: " + fileUrl);
        } catch (IOException e) {
            LOGGER.error("Error occurred while loading file: " + fileUrl, e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
            .add(WebConfigConstants.HADOOP_URL, nameNodeUrl)
            .add(WebConfigConstants.ZK_INSTANCENAME, zkInstanceName)
            .add(WebConfigConstants.ZK_SERVERS, zkServerNames)
            .add(WebConfigConstants.ACCUMULO_USER, dataStoreUserName)
            .add(WebConfigConstants.ACCUMULO_PASSWORD, dataStorePassword)
            .add(WebConfigConstants.BLUR_CONTROLLER, searchIndexController)
            .add(WebConfigConstants.BLUR_PATH, searchIndexStoragePath)
            .add(WebConfigConstants.GRAPH_SEARCH_HOSTNAME, graphSearchIndexHostname)
            .add(WebConfigConstants.MAP_PROVIDER, mapProvider)
            .add(WebConfigConstants.MAP_ACCESS_KEY, mapAccessKey)
            .add(WebConfigConstants.MAP_TILE_SERVER_HOST, mapTileServerHostname)
            .add(WebConfigConstants.MAP_TILE_SERVER_PORT, mapTileServerPort)
            .add(WebConfigConstants.SEARCH_PROVIDER, searchProvider)
            .add(WebConfigConstants.ELASTIC_SEARCH_LOCATIONS, elasticSearchLocations)
            .toString();
    }
}
