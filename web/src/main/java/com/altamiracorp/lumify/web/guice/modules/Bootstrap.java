package com.altamiracorp.lumify.web.guice.modules;

import static com.google.common.base.Preconditions.checkNotNull;

import com.altamiracorp.lumify.BootstrapBase;
import com.altamiracorp.lumify.config.ApplicationConfig;
import com.altamiracorp.lumify.config.ConfigConstants;
import com.altamiracorp.lumify.config.Configuration;
import com.altamiracorp.lumify.config.MapConfig;
import com.altamiracorp.lumify.web.AuthenticationProvider;

/**
 * Wires up the required injections for the web application
 */
public class Bootstrap extends BootstrapBase {

    private final Configuration configuration;

    public Bootstrap(final Configuration config) {
        super(config.getProperties(), null);
        checkNotNull(config);

        configuration = config;
    }

    @Override
    protected void configure() {
        bind(MapConfig.class).toInstance(configuration);
        bind(ApplicationConfig.class).toInstance(configuration);
        bind(AuthenticationProvider.class).to(getAuthenticationProviderClass());
    }

    private Class<AuthenticationProvider> getAuthenticationProviderClass() {
        String authProviderClass = configuration.getAuthenticationProvider();
        if (authProviderClass == null) {
            throw new RuntimeException("No " + ConfigConstants.AUTHENTICATION_PROVIDER + " config property set.");
        }

        try {
            return (Class<AuthenticationProvider>) Class.forName(authProviderClass);
        } catch (Exception e) {
            throw new RuntimeException("Unable to create AuthenticationProvider with class name " + authProviderClass, e);
        }
    }
}
