package com.altamiracorp.lumify.clavin;

import com.altamiracorp.lumify.core.bootstrap.BootstrapBindingProvider;
import com.altamiracorp.lumify.core.config.Configuration;
import com.google.inject.Binder;
import com.google.inject.Scopes;

/**
 * Bootstrap bindings for Clavin support.
 */
public class ClavinBootstrapBindingProvider implements BootstrapBindingProvider {
    @Override
    public void addBindings(Binder binder, Configuration configuration) {
        binder.bind(ClavinOntologyMapper.class).to(SimpleClavinOntologyMapper.class).in(Scopes.SINGLETON);
    }
}
