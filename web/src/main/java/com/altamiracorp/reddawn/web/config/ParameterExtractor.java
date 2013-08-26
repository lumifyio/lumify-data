package com.altamiracorp.reddawn.web.config;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Properties;

import javax.servlet.ServletContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Extracts and stores web context parameters
 */
public final class ParameterExtractor {
    private static final Logger LOGGER = LoggerFactory.getLogger(ParameterExtractor.class.getName());

    private final ServletContext servletContext;
    private final Properties applicationProps = new Properties();

    public ParameterExtractor(final ServletContext context) {
        checkNotNull(context);
        servletContext = context;
    }


    /**
     * Attempts to extract the provided web context parameter and store it as a property with
     * the specified name
     * @param parameter The name of the context parameter to extract, not null or empty
     * @param property The name of the property used to store the extracted value, not null or empty
     */
    public void extractParamAsProperty(final String parameter, final String property) {
        checkNotNull(parameter);
        checkArgument(!parameter.isEmpty());
        checkNotNull(property);
        checkArgument(!property.isEmpty());

        final String contextParamValue = servletContext.getInitParameter(parameter);

        if( contextParamValue != null ) {
            applicationProps.setProperty(property, contextParamValue);
            LOGGER.info(String.format("Extracted context initialization parameter: %s=%s", parameter, contextParamValue));
        } else {
            LOGGER.warn(String.format("Could not find expected parameter: %s", parameter));
        }
    }

    public Properties getApplicationProperties() {
        return applicationProps;
    }
}
