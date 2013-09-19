package com.altamiracorp.lumify.web.guice.modules;

import com.altamiracorp.lumify.config.ApplicationConfig;
import com.altamiracorp.lumify.config.ConfigConstants;
import com.altamiracorp.lumify.config.Configuration;
import com.altamiracorp.lumify.config.MapConfig;
import com.altamiracorp.lumify.core.user.SystemUser;
import com.altamiracorp.lumify.core.user.User;
import com.altamiracorp.lumify.model.*;
import com.altamiracorp.lumify.model.graph.GraphRepository;
import com.altamiracorp.lumify.model.ontology.OntologyRepository;
import com.altamiracorp.lumify.model.termMention.TermMentionRepository;
import com.altamiracorp.lumify.model.user.UserRepository;
import com.altamiracorp.lumify.model.workspace.WorkspaceRepository;
import com.altamiracorp.lumify.search.ElasticSearchProvider;
import com.altamiracorp.lumify.search.SearchProvider;
import com.altamiracorp.lumify.ucd.artifact.ArtifactRepository;
import com.altamiracorp.lumify.web.AuthenticationProvider;
import com.google.inject.AbstractModule;
import com.google.inject.Singleton;
import org.apache.accumulo.core.client.Connector;
import org.apache.accumulo.core.client.ZooKeeperInstance;
import org.apache.hadoop.fs.FileSystem;

import java.net.URI;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Wires up the required injections for the web application
 */
public class Bootstrap extends AbstractModule {

    private final Configuration configuration;
    private static final String DEFAULT_SEARCH_PROVIDER = ElasticSearchProvider.class.getName();

    public Bootstrap(final Configuration config) {
        checkNotNull(config);

        configuration = config;
    }

    @Override
    protected void configure() {
        User user = new SystemUser();

        bind(MapConfig.class).toInstance(configuration);
        bind(ApplicationConfig.class).toInstance(configuration);
        bind(AuthenticationProvider.class).to(getAuthenticationProviderClass());
        bind(ModelSession.class).toInstance(createModelSession());
        bind(GraphSession.class).toInstance(createGraphSession());
        bind(SearchProvider.class).toInstance(createSearchProvider(user));

        bind(WorkspaceRepository.class).in(Singleton.class);
        bind(UserRepository.class).in(Singleton.class);
        bind(TermMentionRepository.class).in(Singleton.class);
        bind(ArtifactRepository.class).in(Singleton.class);
        bind(OntologyRepository.class).in(Singleton.class);
        bind(GraphRepository.class).in(Singleton.class);
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

    private ModelSession createModelSession() {
        final String hdfsRootDir = configuration.getNamenodeUrl();
        final ZooKeeperInstance zooKeeperInstance = new ZooKeeperInstance(configuration.getZookeeperInstanceName(), configuration.getZookeeperServerNames());
        final org.apache.hadoop.conf.Configuration hadoopConfiguration = new org.apache.hadoop.conf.Configuration();

        try {
            final Connector connector = zooKeeperInstance.getConnector(configuration.getDataStoreUserName(), configuration.getDataStorePassword());
            final FileSystem hdfsFileSystem = FileSystem.get(new URI(hdfsRootDir), hadoopConfiguration, "hadoop");
            return new AccumuloSession(connector, hdfsFileSystem, hdfsRootDir, null);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private GraphSession createGraphSession() {
        return new TitanGraphSession(configuration.getProperties(), new TitanQueryFormatter());
    }

    private SearchProvider createSearchProvider(com.altamiracorp.lumify.core.user.User user) {
        String providerClass = DEFAULT_SEARCH_PROVIDER;
        final String searchProviderName = configuration.getSearchProvider();

        if (searchProviderName != null && !searchProviderName.isEmpty()) {
            providerClass = searchProviderName;
        }

        try {
            SearchProvider provider = (SearchProvider) Class.forName(providerClass).newInstance();
            provider.setup(configuration.getProperties(), user);
            return provider;
        } catch (Exception e) {
            throw new RuntimeException("Failed to create search provider instance of class " + providerClass, e);
        }
    }
}
