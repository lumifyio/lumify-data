package com.altamiracorp.lumify.web;

import com.altamiracorp.lumify.AppSession;
import com.altamiracorp.lumify.config.ConfigConstants;
import com.altamiracorp.lumify.config.Configuration;
import com.altamiracorp.lumify.web.config.ParameterExtractor;
import com.altamiracorp.lumify.web.guice.modules.Bootstrap;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.util.Properties;

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

        if (context != null) {
            final Configuration config = fetchApplicationConfiguration(context);
            LOGGER.info("Running application with configuration: " + config);

            final Injector injector = Guice.createInjector(new Bootstrap(config));

            // Store the injector in the context for a servlet to access later
            context.setAttribute(Injector.class.getName(), injector);

            setupSession(config);

        } else {
            LOGGER.error("Servlet context could not be acquired!");
        }

        AppSession session = AppSession.create();
        session.initialize();
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        LOGGER.info("Servlet context destroyed...");
    }


    private Configuration fetchApplicationConfiguration(final ServletContext context) {
        // Extract the required context parameters from the deployment descriptor
        final ParameterExtractor extractor = new ParameterExtractor(context);
        extractor.extractParamAsProperty(ConfigConstants.APP_CONFIG_LOCATION, ConfigConstants.APP_CONFIG_LOCATION);
        extractor.extractParamAsProperty(ConfigConstants.APP_CREDENTIALS_LOCATION, ConfigConstants.APP_CREDENTIALS_LOCATION);

        final Properties appConfigProps = extractor.getApplicationProperties();

        // Find the location of the application configuration and credential files and process them
        final String configLocation = appConfigProps.getProperty(ConfigConstants.APP_CONFIG_LOCATION);
        final String credentialsLocation = appConfigProps.getProperty(ConfigConstants.APP_CREDENTIALS_LOCATION);

        return Configuration.loadConfigurationFile(configLocation, credentialsLocation);
    }


    private void setupSession(final Configuration config) {
        AppSession.setApplicationProperties(config.getProperties());
    }
}
