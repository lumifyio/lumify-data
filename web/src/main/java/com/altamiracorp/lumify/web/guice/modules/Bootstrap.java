package com.altamiracorp.lumify.web.guice.modules;

import com.altamiracorp.lumify.web.config.ApplicationConfig;
import com.altamiracorp.lumify.web.config.Configuration;
import com.altamiracorp.lumify.web.config.MapConfig;
import com.google.inject.AbstractModule;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Wires up the required injections for the web application
 */
public class Bootstrap extends AbstractModule {

    private final Configuration configuration;

    public Bootstrap(final Configuration config) {
        checkNotNull(config);

        configuration = config;
    }

    @Override
    protected void configure() {
        bind(MapConfig.class).toInstance(configuration);
        bind(ApplicationConfig.class).toInstance(configuration);
    }

}
