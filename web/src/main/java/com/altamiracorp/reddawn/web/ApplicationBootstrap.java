package com.altamiracorp.reddawn.web;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
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
import com.altamiracorp.reddawn.web.config.ParameterExtractor;
import com.altamiracorp.reddawn.web.config.WebContextConstants;

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
            final ParameterExtractor extractor = new ParameterExtractor(context);

            extractor.extractParamAsProperty(WebContextConstants.HADOOP_URL, AccumuloSession.HADOOP_URL);
            extractor.extractParamAsProperty(WebContextConstants.ZK_INSTANCENAME, AccumuloSession.ZOOKEEPER_INSTANCE_NAME);
            extractor.extractParamAsProperty(WebContextConstants.ZK_SERVERS, AccumuloSession.ZOOKEEPER_SERVER_NAMES);
            extractor.extractParamAsProperty(WebContextConstants.ACCUMULO_USER, AccumuloSession.USERNAME);
            extractor.extractParamAsProperty(WebContextConstants.ACCUMULO_PASSWORD, AccumuloSession.PASSWORD);
            extractor.extractParamAsProperty(WebContextConstants.BLUR_CONTROLLER, BlurSearchProvider.BLUR_CONTROLLER_LOCATION);
            extractor.extractParamAsProperty(WebContextConstants.BLUR_PATH, BlurSearchProvider.BLUR_PATH);
            extractor.extractParamAsProperty(WebContextConstants.GRAPH_SEARCH_HOSTNAME, TitanGraphSession.STORAGE_INDEX_SEARCH_HOSTNAME);
            extractor.extractParamAsProperty(WebContextConstants.APP_CONFIG_PATH, WebContextConstants.APP_CONFIG_PATH);


            final Properties appConfigProps = extractor.getApplicationProperties();

            // Find the location of the application configuration file and process it
            final String filePath = appConfigProps.getProperty(WebContextConstants.APP_CONFIG_PATH);
            final Properties loadedProps = loadConfigurationFile(filePath);

            if( !loadedProps.isEmpty() ) {
                LOGGER.info("Using properties extracted from configuration file");
                RedDawnSession.setApplicationProperties(loadedProps);
            } else {
                LOGGER.info("Using properties extracted from deployment descriptor file");
                RedDawnSession.setApplicationProperties(appConfigProps);
            }
        } else {
            LOGGER.warn("Servlet context could not be acquired!");
        }

        RedDawnSession.create().getModelSession().initializeTables();
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        LOGGER.info("Servlet context destroyed...");
    }


    private Properties loadConfigurationFile(final String filePath) {
        Properties prop = new Properties();

        if( filePath != null && !filePath.isEmpty() ) {
            try {
                prop.load(new FileInputStream(filePath));

                // Extract the configuration properties required by the session and store them with their expected keys
                final Properties sessionProps = new Properties();
                sessionProps.setProperty(AccumuloSession.HADOOP_URL, prop.getProperty(WebContextConstants.HADOOP_URL));
                sessionProps.setProperty(AccumuloSession.ZOOKEEPER_INSTANCE_NAME, prop.getProperty(WebContextConstants.ZK_INSTANCENAME));
                sessionProps.setProperty(AccumuloSession.ZOOKEEPER_SERVER_NAMES, prop.getProperty(WebContextConstants.ZK_SERVERS));
                sessionProps.setProperty(AccumuloSession.USERNAME, prop.getProperty(WebContextConstants.ACCUMULO_USER));
                sessionProps.setProperty(AccumuloSession.PASSWORD, prop.getProperty(WebContextConstants.ACCUMULO_PASSWORD));
                sessionProps.setProperty(BlurSearchProvider.BLUR_CONTROLLER_LOCATION, prop.getProperty(WebContextConstants.BLUR_CONTROLLER));
                sessionProps.setProperty(BlurSearchProvider.BLUR_PATH, prop.getProperty(WebContextConstants.BLUR_PATH));
                sessionProps.setProperty(TitanGraphSession.STORAGE_INDEX_SEARCH_HOSTNAME, prop.getProperty(WebContextConstants.GRAPH_SEARCH_HOSTNAME));

                prop = sessionProps;
            } catch (FileNotFoundException e) {
                LOGGER.warn("Could not find file to load at: " + filePath, e);
            } catch (IOException e) {
                LOGGER.warn("Error occurred while loading file", e);
            }
        }

        return prop;
    }
}
