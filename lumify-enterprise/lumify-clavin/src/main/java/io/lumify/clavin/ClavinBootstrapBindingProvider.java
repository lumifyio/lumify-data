package io.lumify.clavin;

import io.lumify.core.bootstrap.BootstrapBindingProvider;
import io.lumify.core.config.Configuration;
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
