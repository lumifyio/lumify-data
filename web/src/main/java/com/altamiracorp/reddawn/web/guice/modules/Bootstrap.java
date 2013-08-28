package com.altamiracorp.reddawn.web.guice.modules;

import static com.google.common.base.Preconditions.checkNotNull;

import com.altamiracorp.reddawn.web.config.ApplicationConfig;
import com.altamiracorp.reddawn.web.config.Configuration;
import com.altamiracorp.reddawn.web.config.MapConfig;
import com.google.inject.AbstractModule;

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
