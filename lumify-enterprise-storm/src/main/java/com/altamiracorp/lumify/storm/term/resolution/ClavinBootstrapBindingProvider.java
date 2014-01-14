/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.altamiracorp.lumify.storm.term.resolution;

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
