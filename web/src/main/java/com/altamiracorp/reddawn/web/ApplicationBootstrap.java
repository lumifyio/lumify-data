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
import com.altamiracorp.reddawn.web.config.ApplicationConfig;
import com.altamiracorp.reddawn.web.config.Configuration;
import com.altamiracorp.reddawn.web.config.ParameterExtractor;
import com.altamiracorp.reddawn.web.config.WebConfigConstants;
import com.altamiracorp.reddawn.web.guice.modules.Bootstrap;
import com.google.inject.Guice;
import com.google.inject.Injector;

/**
 * Responsible for defining behavior corresponding to web servlet context
 * initialization and destruction
 */
public final class ApplicationBootstrap implements ServletContextListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationBootstrap.class.getName());

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        LOGGER.info("Servlet context initialized...");

        final ServletContext context = sce.getServletContext();

        if( context != null ) {
            final Configuration config = fetchApplicationConfiguration(context);
            final Injector injector = Guice.createInjector(new Bootstrap(config));

            // Store the injector in the context for a servlet to access later
            context.setAttribute(Injector.class.getName(), injector);

            setupSession(config);

        } else {
            LOGGER.error("Servlet context could not be acquired!");
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        LOGGER.info("Servlet context destroyed...");
    }


    private Configuration fetchApplicationConfiguration(final ServletContext context) {
        // Extract the required context parameters from the deployment descriptor
        final ParameterExtractor extractor = new ParameterExtractor(context);
        extractor.extractParamAsProperty(WebConfigConstants.APP_CONFIG_PATH, WebConfigConstants.APP_CONFIG_PATH);

        final Properties appConfigProps = extractor.getApplicationProperties();

        // Find the location of the application configuration file and process it
        final String filePath = appConfigProps.getProperty(WebConfigConstants.APP_CONFIG_PATH);

        return Configuration.loadConfigurationFile(filePath);
    }


    private void setupSession(final ApplicationConfig config) {
        final Properties props = new Properties();
        props.setProperty(AccumuloSession.HADOOP_URL, config.getNamenodeUrl());
        props.setProperty(AccumuloSession.ZOOKEEPER_INSTANCE_NAME, config.getZookeeperInstanceName());
        props.setProperty(AccumuloSession.ZOOKEEPER_SERVER_NAMES, config.getZookeeperServerNames());
        props.setProperty(AccumuloSession.USERNAME, config.getDataStoreUserName());
        props.setProperty(AccumuloSession.PASSWORD, config.getDataStorePassword());
        props.setProperty(BlurSearchProvider.BLUR_CONTROLLER_LOCATION, config.getSearchIndexController());
        props.setProperty(BlurSearchProvider.BLUR_PATH, config.getSearchIndexStoragePath());
        props.setProperty(TitanGraphSession.STORAGE_INDEX_SEARCH_HOSTNAME, config.getGraphSearchIndexHostname());

        RedDawnSession.setApplicationProperties(props);
        RedDawnSession.create().getModelSession().initializeTables();
    }
}
