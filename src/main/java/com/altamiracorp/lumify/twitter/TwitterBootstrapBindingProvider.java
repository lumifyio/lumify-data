package com.altamiracorp.lumify.twitter;

import com.altamiracorp.lumify.core.bootstrap.BootstrapBindingProvider;
import com.altamiracorp.lumify.core.config.Configuration;
import com.google.inject.Binder;
import com.google.inject.Scopes;

/**
 * This class provides Guice bindings for Twitter classes.
 */
public class TwitterBootstrapBindingProvider implements BootstrapBindingProvider {
    @Override
    public void addBindings(final Binder binder, final Configuration configuration) {
        binder.bind(LumifyTwitterProcessor.class).to(DefaultLumifyTwitterProcessor.class).in(Scopes.SINGLETON);
        binder.bind(UrlStreamCreator.class).to(URLUrlStreamCreator.class).in(Scopes.SINGLETON);
    }
}
