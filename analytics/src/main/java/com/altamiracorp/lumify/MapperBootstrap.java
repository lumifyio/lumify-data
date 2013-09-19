package com.altamiracorp.lumify;

import com.altamiracorp.lumify.core.user.SystemUser;
import com.altamiracorp.lumify.core.user.User;
import com.altamiracorp.lumify.model.*;
import com.altamiracorp.lumify.search.ElasticSearchProvider;
import com.altamiracorp.lumify.search.SearchProvider;
import com.google.inject.AbstractModule;
import org.apache.accumulo.core.client.Connector;
import org.apache.accumulo.core.client.ZooKeeperInstance;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.mapreduce.TaskInputOutputContext;

import java.net.URI;
import java.util.Map;
import java.util.Properties;

import static com.google.common.base.Preconditions.checkNotNull;

public class MapperBootstrap extends AbstractModule {

    private final Configuration configuration;
    private final TaskInputOutputContext attemptContext;
    private final Properties properties = new Properties();
    private static final String DEFAULT_SEARCH_PROVIDER = ElasticSearchProvider.class.getName();

    public MapperBootstrap(final TaskInputOutputContext context) {
        checkNotNull(context);

        attemptContext = context;
        configuration = context.getConfiguration();

        for (Map.Entry<String, String> entry : configuration) {
            properties.setProperty(entry.getKey(), entry.getValue());
        }
    }

    @Override
    protected void configure() {
        User user = new SystemUser();

        bind(ModelSession.class).toInstance(createModelSession());
        bind(GraphSession.class).toInstance(createGraphSession());
        bind(SearchProvider.class).toInstance(createSearchProvider(user));
    }


    private ModelSession createModelSession() {
        final String hdfsRootDir = configuration.get(AccumuloSession.HADOOP_URL);
        final ZooKeeperInstance zooKeeperInstance = new ZooKeeperInstance(configuration.get(AccumuloSession.ZOOKEEPER_INSTANCE_NAME), configuration.get(AccumuloSession.ZOOKEEPER_SERVER_NAMES));
        final org.apache.hadoop.conf.Configuration hadoopConfiguration = new org.apache.hadoop.conf.Configuration();

        try {
            final Connector connector = zooKeeperInstance.getConnector(configuration.get(AccumuloSession.USERNAME), configuration.get(AccumuloSession.PASSWORD));
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
        final String searchProviderName = configuration.get(SearchProvider.SEARCH_PROVIDER_PROP_KEY);

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
