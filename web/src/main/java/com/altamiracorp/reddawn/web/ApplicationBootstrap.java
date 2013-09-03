package com.altamiracorp.reddawn.web;

import java.util.Properties;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.altamiracorp.reddawn.RedDawnSession;
import com.altamiracorp.reddawn.model.AccumuloSession;
import com.altamiracorp.reddawn.model.TitanGraphSession;
import com.altamiracorp.reddawn.search.BlurSearchProvider;
import com.altamiracorp.reddawn.search.ElasticSearchProvider;
import com.altamiracorp.reddawn.web.config.ApplicationConfig;
import com.altamiracorp.reddawn.web.config.Configuration;
import com.altamiracorp.reddawn.web.config.ParameterExtractor;
import com.altamiracorp.reddawn.web.config.PropertyUtils;
import com.altamiracorp.reddawn.web.config.WebConfigConstants;
import com.altamiracorp.reddawn.web.guice.modules.Bootstrap;
import com.google.inject.Guice;
import com.google.inject.Injector;

/**
 * Responsible for defining behavior corresponding to web servlet context
 * initialization and destruction
 */
public final class ApplicationBootstrap implements ServletContextListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationBootstrap.class);

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        LOGGER.info("Servlet context initialized...");

        final ServletContext context = sce.getServletContext();

        if( context != null ) {
            final Configuration config = fetchApplicationConfiguration(context);
            LOGGER.info("Running application with configuration: " + config);

            final Injector injector = Guice.createInjector(new Bootstrap(config));

            // Store the injector in the context for a servlet to access later
            context.setAttribute(Injector.class.getName(), injector);

            setupSession(config);

        } else {
            LOGGER.error("Servlet context could not be acquired!");
        }

        RedDawnSession redDawnSession = RedDawnSession.create();
        redDawnSession.initialize();
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        LOGGER.info("Servlet context destroyed...");
    }


    private Configuration fetchApplicationConfiguration(final ServletContext context) {
        // Extract the required context parameters from the deployment descriptor
        final ParameterExtractor extractor = new ParameterExtractor(context);
        extractor.extractParamAsProperty(WebConfigConstants.APP_CONFIG_LOCATION, WebConfigConstants.APP_CONFIG_LOCATION);
        extractor.extractParamAsProperty(WebConfigConstants.APP_CREDENTIALS_LOCATION, WebConfigConstants.APP_CREDENTIALS_LOCATION);

        final Properties appConfigProps = extractor.getApplicationProperties();

        // Find the location of the application configuration and credential files and process them
        final String configLocation = appConfigProps.getProperty(WebConfigConstants.APP_CONFIG_LOCATION);
        final String credentialsLocation = appConfigProps.getProperty(WebConfigConstants.APP_CREDENTIALS_LOCATION);

        return Configuration.loadConfigurationFile(configLocation, credentialsLocation);
    }


    private void setupSession(final ApplicationConfig config) {
        final Properties props = new Properties();
        PropertyUtils.setPropertyValue(props, AccumuloSession.HADOOP_URL, config.getNamenodeUrl());
        PropertyUtils.setPropertyValue(props, AccumuloSession.ZOOKEEPER_INSTANCE_NAME, config.getZookeeperInstanceName());
        PropertyUtils.setPropertyValue(props, AccumuloSession.ZOOKEEPER_SERVER_NAMES, config.getZookeeperServerNames());
        PropertyUtils.setPropertyValue(props, AccumuloSession.USERNAME, config.getDataStoreUserName());
        PropertyUtils.setPropertyValue(props, AccumuloSession.PASSWORD, config.getDataStorePassword());
        PropertyUtils.setPropertyValue(props, BlurSearchProvider.BLUR_CONTROLLER_LOCATION, config.getSearchIndexController());
        PropertyUtils.setPropertyValue(props, BlurSearchProvider.BLUR_PATH, config.getSearchIndexStoragePath());
        PropertyUtils.setPropertyValue(props, TitanGraphSession.STORAGE_INDEX_SEARCH_HOSTNAME, config.getGraphSearchIndexHostname());
        PropertyUtils.setPropertyValue(props, RedDawnSession.SEARCH_PROVIDER_PROP_KEY, config.getSearchProvider());
        PropertyUtils.setPropertyValue(props, ElasticSearchProvider.ES_LOCATIONS_PROP_KEY, config.getElasticSearchLocations());

        RedDawnSession.setApplicationProperties(props);
        RedDawnSession.create().getModelSession().initializeTables();
    }
}
