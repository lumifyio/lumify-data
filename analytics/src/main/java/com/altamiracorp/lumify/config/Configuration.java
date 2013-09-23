package com.altamiracorp.lumify.config;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.altamiracorp.lumify.model.AccumuloSession;
import com.altamiracorp.lumify.model.TitanGraphSession;
import com.altamiracorp.lumify.search.BlurSearchProvider;
import com.altamiracorp.lumify.search.ElasticSearchProvider;
import com.altamiracorp.lumify.search.SearchProvider;
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
    private static String authenticationProvider;
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
    public String getAuthenticationProvider() {
        return authenticationProvider;
    }

    @Override
    public int getMapTileServerPort() {
        return mapTileServerPort;
    }

    /**
     * Attempts to parse the application configuration file located at the
     * specified filesystem path
     *
     * @param configUrl      The URL to the configuration file, not null or empty
     * @param credentialsUrl The URL to the credentials file, not null or empty
     * @return A {@link Configuration} object that contains the parsed configuration values
     */
    public static Configuration loadConfigurationFile(final String configUrl, final String credentialsUrl) {
        checkNotNull(configUrl, "The specified config file URL was null");
        checkArgument(!configUrl.isEmpty(), "The specified config file URL was empty");

        LOGGER.debug(String.format("Attempting to load configuration file: %s and credentials file: %s", configUrl, credentialsUrl));

        final Properties mergedProps = new Properties();

        processFile(configUrl, mergedProps);
        if (credentialsUrl != null) {
            processFile(credentialsUrl, mergedProps);
        }

        // Extract the expected configuration properties
        nameNodeUrl = mergedProps.getProperty(ConfigConstants.HADOOP_URL, UNKNOWN_STRING);
        zkInstanceName = mergedProps.getProperty(ConfigConstants.ZK_INSTANCENAME, UNKNOWN_STRING);
        zkServerNames = mergedProps.getProperty(ConfigConstants.ZK_SERVERS, UNKNOWN_STRING);
        dataStoreUserName = mergedProps.getProperty(ConfigConstants.ACCUMULO_USER, UNKNOWN_STRING);
        dataStorePassword = mergedProps.getProperty(ConfigConstants.ACCUMULO_PASSWORD, UNKNOWN_STRING);
        searchIndexController = mergedProps.getProperty(ConfigConstants.BLUR_CONTROLLER, UNKNOWN_STRING);
        searchIndexStoragePath = mergedProps.getProperty(ConfigConstants.BLUR_PATH, UNKNOWN_STRING);
        graphSearchIndexHostname = mergedProps.getProperty(ConfigConstants.GRAPH_SEARCH_HOSTNAME, UNKNOWN_STRING);
        mapProvider = mergedProps.getProperty(ConfigConstants.MAP_PROVIDER, UNKNOWN_STRING);
        mapAccessKey = mergedProps.getProperty(ConfigConstants.MAP_ACCESS_KEY, UNKNOWN_STRING);
        mapTileServerHostname = mergedProps.getProperty(ConfigConstants.MAP_TILE_SERVER_HOST, UNKNOWN_STRING);
        mapTileServerPort = Integer.parseInt(mergedProps.getProperty(ConfigConstants.MAP_TILE_SERVER_PORT, UNKNOWN_INT));
        searchProvider = mergedProps.getProperty(ConfigConstants.SEARCH_PROVIDER);
        elasticSearchLocations = mergedProps.getProperty(ConfigConstants.ELASTIC_SEARCH_LOCATIONS);
        authenticationProvider = mergedProps.getProperty(ConfigConstants.AUTHENTICATION_PROVIDER);

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
                .add(ConfigConstants.HADOOP_URL, nameNodeUrl)
                .add(ConfigConstants.ZK_INSTANCENAME, zkInstanceName)
                .add(ConfigConstants.ZK_SERVERS, zkServerNames)
                .add(ConfigConstants.ACCUMULO_USER, dataStoreUserName)
                .add(ConfigConstants.ACCUMULO_PASSWORD, dataStorePassword)
                .add(ConfigConstants.BLUR_CONTROLLER, searchIndexController)
                .add(ConfigConstants.BLUR_PATH, searchIndexStoragePath)
                .add(ConfigConstants.GRAPH_SEARCH_HOSTNAME, graphSearchIndexHostname)
                .add(ConfigConstants.MAP_PROVIDER, mapProvider)
                .add(ConfigConstants.MAP_ACCESS_KEY, mapAccessKey)
                .add(ConfigConstants.MAP_TILE_SERVER_HOST, mapTileServerHostname)
                .add(ConfigConstants.MAP_TILE_SERVER_PORT, mapTileServerPort)
                .add(ConfigConstants.SEARCH_PROVIDER, searchProvider)
                .add(ConfigConstants.ELASTIC_SEARCH_LOCATIONS, elasticSearchLocations)
                .add(ConfigConstants.AUTHENTICATION_PROVIDER, authenticationProvider)
                .toString();
    }

    public Properties getProperties() {
        final Properties props = new Properties();
        PropertyUtils.setPropertyValue(props, AccumuloSession.HADOOP_URL, getNamenodeUrl());
        PropertyUtils.setPropertyValue(props, AccumuloSession.ZOOKEEPER_INSTANCE_NAME, getZookeeperInstanceName());
        PropertyUtils.setPropertyValue(props, AccumuloSession.ZOOKEEPER_SERVER_NAMES, getZookeeperServerNames());
        PropertyUtils.setPropertyValue(props, AccumuloSession.USERNAME, getDataStoreUserName());
        PropertyUtils.setPropertyValue(props, AccumuloSession.PASSWORD, getDataStorePassword());
        PropertyUtils.setPropertyValue(props, BlurSearchProvider.BLUR_CONTROLLER_LOCATION, getSearchIndexController());
        PropertyUtils.setPropertyValue(props, BlurSearchProvider.BLUR_PATH, getSearchIndexStoragePath());
        PropertyUtils.setPropertyValue(props, TitanGraphSession.STORAGE_INDEX_SEARCH_HOSTNAME, getGraphSearchIndexHostname());
        PropertyUtils.setPropertyValue(props, SearchProvider.SEARCH_PROVIDER_PROP_KEY, getSearchProvider());
        PropertyUtils.setPropertyValue(props, ElasticSearchProvider.ES_LOCATIONS_PROP_KEY, getElasticSearchLocations());
        return props;
    }
}
