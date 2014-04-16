package com.altamiracorp.lumify.web;

import com.altamiracorp.lumify.core.bootstrap.BootstrapBindingProvider;
import com.altamiracorp.lumify.core.config.Configuration;
import com.altamiracorp.lumify.core.exception.LumifyException;
import com.altamiracorp.lumify.ldap.LdapSearchConfiguration;
import com.altamiracorp.lumify.ldap.LdapSearchService;
import com.altamiracorp.lumify.ldap.LdapSearchServiceImpl;
import com.altamiracorp.lumify.ldap.LdapServerConfiguration;
import com.google.inject.Binder;
import com.google.inject.Provider;
import com.google.inject.Scopes;

public class LdapX509AuthenticationProviderBootstrapBindingProvider implements BootstrapBindingProvider {
    @Override
    public void addBindings(Binder binder, final Configuration configuration) {
        binder.bind(LdapSearchService.class)
                .toProvider(new Provider<LdapSearchService>() {
                    @Override
                    public LdapSearchService get() {
                        LdapServerConfiguration ldapServerConfiguration = new LdapServerConfiguration();
                        configuration.setConfigurables(ldapServerConfiguration, "ldap");

                        LdapSearchConfiguration ldapSearchConfiguration = new LdapSearchConfiguration();
                        configuration.setConfigurables(ldapSearchConfiguration, "ldap");

                        try {
                            return new LdapSearchServiceImpl(ldapServerConfiguration, ldapSearchConfiguration);
                        } catch (Exception e) {
                            throw new LumifyException("failed to configure ldap search service", e);
                        }
                    }
                })
                .in(Scopes.SINGLETON);
    }
}
