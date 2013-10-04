package com.altamiracorp.lumify.config;

import com.altamiracorp.lumify.model.AccumuloSession;
import com.altamiracorp.lumify.model.TitanGraphSession;
import com.altamiracorp.lumify.model.search.SearchProvider;
import com.altamiracorp.lumify.search.BlurSearchProvider;
import com.altamiracorp.lumify.search.ElasticSearchProvider;
import com.google.common.base.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

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
    private final Properties properties;

    private Configuration(Properties properties) {
        this.properties = properties;
    }

    @Override
    public String getNamenodeUrl() {
        return properties.getProperty(ConfigConstants.HADOOP_URL, UNKNOWN_STRING);
    }

    @Override
    public String getZookeeperInstanceName() {
        return properties.getProperty(ConfigConstants.ZK_INSTANCENAME, UNKNOWN_STRING);
    }

    @Override
    public String getZookeeperServerNames() {
        return properties.getProperty(ConfigConstants.ZK_SERVERS, UNKNOWN_STRING);
    }

    @Override
    public String getDataStoreUserName() {
        return properties.getProperty(ConfigConstants.ACCUMULO_USER, UNKNOWN_STRING);
    }

    @Override
    public String getDataStorePassword() {
        return properties.getProperty(ConfigConstants.ACCUMULO_PASSWORD, UNKNOWN_STRING);
    }

    @Override
    public String getSearchIndexController() {
        return properties.getProperty(ConfigConstants.BLUR_CONTROLLER, UNKNOWN_STRING);
    }

    @Override
    public String getSearchIndexStoragePath() {
        return properties.getProperty(ConfigConstants.BLUR_PATH, UNKNOWN_STRING);
    }

    @Override
    public String getGraphSearchIndexHostname() {
        return properties.getProperty(ConfigConstants.GRAPH_SEARCH_HOSTNAME, UNKNOWN_STRING);
    }

    @Override
    public String getSearchProvider() {
        return properties.getProperty(ConfigConstants.SEARCH_PROVIDER);
    }

    @Override
    public String getElasticSearchLocations() {
        return properties.getProperty(ConfigConstants.ELASTIC_SEARCH_LOCATIONS);
    }

    @Override
    public String getMapProvider() {
        return properties.getProperty(ConfigConstants.MAP_PROVIDER, UNKNOWN_STRING);
    }

    @Override
    public String getMapAccessKey() {
        return properties.getProperty(ConfigConstants.MAP_ACCESS_KEY, UNKNOWN_STRING);
    }

    @Override
    public String getMapTileServerHostname() {
        return properties.getProperty(ConfigConstants.MAP_TILE_SERVER_HOST, UNKNOWN_STRING);
    }

    @Override
    public String getAuthenticationProvider() {
        return properties.getProperty(ConfigConstants.AUTHENTICATION_PROVIDER);
    }

    @Override
    public int getMapTileServerPort() {
        return Integer.parseInt(properties.getProperty(ConfigConstants.MAP_TILE_SERVER_PORT, UNKNOWN_INT));
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

        Properties properties = new Properties();

        processFile(configUrl, properties);
        if (credentialsUrl != null) {
            processFile(credentialsUrl, properties);
        }

        return new Configuration(properties);
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
                .add(ConfigConstants.HADOOP_URL, getNamenodeUrl())
                .add(ConfigConstants.ZK_INSTANCENAME, getZookeeperInstanceName())
                .add(ConfigConstants.ZK_SERVERS, getZookeeperServerNames())
                .add(ConfigConstants.ACCUMULO_USER, getDataStoreUserName())
                .add(ConfigConstants.ACCUMULO_PASSWORD, getDataStorePassword())
                .add(ConfigConstants.BLUR_CONTROLLER, getSearchIndexController())
                .add(ConfigConstants.BLUR_PATH, getSearchIndexStoragePath())
                .add(ConfigConstants.GRAPH_SEARCH_HOSTNAME, getGraphSearchIndexHostname())
                .add(ConfigConstants.MAP_PROVIDER, getMapProvider())
                .add(ConfigConstants.MAP_ACCESS_KEY, getMapAccessKey())
                .add(ConfigConstants.MAP_TILE_SERVER_HOST, getMapTileServerHostname())
                .add(ConfigConstants.MAP_TILE_SERVER_PORT, getMapTileServerPort())
                .add(ConfigConstants.SEARCH_PROVIDER, getSearchProvider())
                .add(ConfigConstants.ELASTIC_SEARCH_LOCATIONS, getElasticSearchLocations())
                .add(ConfigConstants.AUTHENTICATION_PROVIDER, getAuthenticationProvider())
                .toString();
    }

    public Properties getProperties() {
        PropertyUtils.setPropertyValue(properties, AccumuloSession.HADOOP_URL, getNamenodeUrl());
        PropertyUtils.setPropertyValue(properties, AccumuloSession.ZOOKEEPER_INSTANCE_NAME, getZookeeperInstanceName());
        PropertyUtils.setPropertyValue(properties, AccumuloSession.ZOOKEEPER_SERVER_NAMES, getZookeeperServerNames());
        PropertyUtils.setPropertyValue(properties, AccumuloSession.USERNAME, getDataStoreUserName());
        PropertyUtils.setPropertyValue(properties, AccumuloSession.PASSWORD, getDataStorePassword());
        PropertyUtils.setPropertyValue(properties, BlurSearchProvider.BLUR_CONTROLLER_LOCATION, getSearchIndexController());
        PropertyUtils.setPropertyValue(properties, BlurSearchProvider.BLUR_PATH, getSearchIndexStoragePath());
        PropertyUtils.setPropertyValue(properties, TitanGraphSession.STORAGE_INDEX_SEARCH_HOSTNAME, getGraphSearchIndexHostname());
        PropertyUtils.setPropertyValue(properties, SearchProvider.SEARCH_PROVIDER_PROP_KEY, getSearchProvider());
        PropertyUtils.setPropertyValue(properties, ElasticSearchProvider.ES_LOCATIONS_PROP_KEY, getElasticSearchLocations());
        return properties;
    }
}
