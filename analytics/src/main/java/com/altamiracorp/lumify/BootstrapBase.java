package com.altamiracorp.lumify;

import com.altamiracorp.lumify.core.user.SystemUser;
import com.altamiracorp.lumify.core.user.User;
import com.altamiracorp.lumify.model.*;
import com.altamiracorp.lumify.search.ElasticSearchProvider;
import com.altamiracorp.lumify.search.SearchProvider;
import com.google.inject.AbstractModule;
import org.apache.accumulo.core.client.Connector;
import org.apache.accumulo.core.client.ZooKeeperInstance;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.mapreduce.TaskInputOutputContext;

import java.net.URI;
import java.util.Properties;

public abstract class BootstrapBase extends AbstractModule {
    private final TaskInputOutputContext attemptContext;

    private final Properties properties;
    private static final String DEFAULT_SEARCH_PROVIDER = ElasticSearchProvider.class.getName();

    protected BootstrapBase(Properties properties, TaskInputOutputContext attemptContext) {
        this.properties = properties;
        this.attemptContext = attemptContext;
    }

    @Override
    protected void configure() {
        User user = new SystemUser();

        bind(ModelSession.class).toInstance(createModelSession());
        bind(GraphSession.class).toInstance(createGraphSession());
        bind(SearchProvider.class).toInstance(createSearchProvider(user));
    }


    private ModelSession createModelSession() {
        final String hdfsRootDir = properties.getProperty(AccumuloSession.HADOOP_URL);
        final ZooKeeperInstance zooKeeperInstance = new ZooKeeperInstance(properties.getProperty(AccumuloSession.ZOOKEEPER_INSTANCE_NAME), properties.getProperty(AccumuloSession.ZOOKEEPER_SERVER_NAMES));
        final org.apache.hadoop.conf.Configuration hadoopConfiguration = new org.apache.hadoop.conf.Configuration();

        try {
            final Connector connector = zooKeeperInstance.getConnector(properties.getProperty(AccumuloSession.USERNAME), properties.getProperty(AccumuloSession.PASSWORD));
            final FileSystem hdfsFileSystem = FileSystem.get(new URI(hdfsRootDir), hadoopConfiguration, "hadoop");
            return new AccumuloSession(connector, hdfsFileSystem, hdfsRootDir, attemptContext);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private GraphSession createGraphSession() {
        return new TitanGraphSession(properties, new TitanQueryFormatter());
    }

    private SearchProvider createSearchProvider(com.altamiracorp.lumify.core.user.User user) {
        String providerClass = DEFAULT_SEARCH_PROVIDER;
        final String searchProviderName = properties.getProperty(SearchProvider.SEARCH_PROVIDER_PROP_KEY);

        if (searchProviderName != null && !searchProviderName.isEmpty()) {
            providerClass = searchProviderName;
        }

        try {
            SearchProvider provider = (SearchProvider) Class.forName(providerClass).newInstance();
            provider.setup(properties, user);
            return provider;
        } catch (Exception e) {
            throw new RuntimeException("Failed to create search provider instance of class " + providerClass, e);
        }
    }
}
