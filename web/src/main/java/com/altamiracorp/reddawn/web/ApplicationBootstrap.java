package com.altamiracorp.reddawn.web;

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

            RedDawnSession.setApplicationProperties(extractor.getApplicationProperties());
        } else {
            LOGGER.warn("Servlet context could not be acquired!");
        }

        RedDawnSession.create().getModelSession().initializeTables();
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        LOGGER.info("Servlet context destroyed...");
    }
}
