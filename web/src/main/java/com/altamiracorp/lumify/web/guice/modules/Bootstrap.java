package com.altamiracorp.lumify.web.guice.modules;

import static com.google.common.base.Preconditions.checkNotNull;

import com.altamiracorp.lumify.config.ApplicationConfig;
import com.altamiracorp.lumify.config.ConfigConstants;
import com.altamiracorp.lumify.config.Configuration;
import com.altamiracorp.lumify.config.MapConfig;
import com.altamiracorp.lumify.model.Repository;
import com.altamiracorp.lumify.model.graph.GraphRepository;
import com.altamiracorp.lumify.model.ontology.OntologyRepository;
import com.altamiracorp.lumify.model.termMention.TermMention;
import com.altamiracorp.lumify.model.termMention.TermMentionRepository;
import com.altamiracorp.lumify.model.user.User;
import com.altamiracorp.lumify.model.user.UserRepository;
import com.altamiracorp.lumify.model.workspace.Workspace;
import com.altamiracorp.lumify.model.workspace.WorkspaceRepository;
import com.altamiracorp.lumify.ucd.artifact.Artifact;
import com.altamiracorp.lumify.ucd.artifact.ArtifactRepository;
import com.altamiracorp.lumify.web.AuthenticationProvider;
import com.google.inject.AbstractModule;
import com.google.inject.Singleton;
import com.google.inject.TypeLiteral;

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
        bind(AuthenticationProvider.class).toInstance(getAuthenticationProviderInstance());

        bind(new TypeLiteral<Repository<Workspace>>() {}).to(WorkspaceRepository.class).in(Singleton.class);
        bind(new TypeLiteral<Repository<User>>() {}).to(UserRepository.class).in(Singleton.class);
        bind(new TypeLiteral<Repository<TermMention>>() {}).to(TermMentionRepository.class).in(Singleton.class);
        bind(new TypeLiteral<Repository<Artifact>>() {}).to(ArtifactRepository.class).in(Singleton.class);
        bind(ArtifactRepository.class).in(Singleton.class);
        bind(OntologyRepository.class).in(Singleton.class);
        bind(GraphRepository.class).in(Singleton.class);
    }

    private AuthenticationProvider getAuthenticationProviderInstance() {
        String authProviderClass = configuration.getAuthenticationProvider();
        if (authProviderClass == null) {
            throw new RuntimeException("No " + ConfigConstants.AUTHENTICATION_PROVIDER + " config property set.");
        }

        try {
            return (AuthenticationProvider) Class.forName(authProviderClass).newInstance();
        } catch (Exception e) {
            throw new RuntimeException("Unable to create AuthenticationProvider with class name " + authProviderClass, e);
        }
    }

}
