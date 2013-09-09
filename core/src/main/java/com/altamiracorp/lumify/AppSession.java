package com.altamiracorp.lumify;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.Properties;

import org.apache.accumulo.core.client.AccumuloException;
import org.apache.accumulo.core.client.AccumuloSecurityException;
import org.apache.accumulo.core.client.Connector;
import org.apache.accumulo.core.client.ZooKeeperInstance;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.mapreduce.TaskInputOutputContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.altamiracorp.lumify.model.AccumuloQueryUser;
import com.altamiracorp.lumify.model.AccumuloSession;
import com.altamiracorp.lumify.model.GraphSession;
import com.altamiracorp.lumify.model.Session;
import com.altamiracorp.lumify.model.TitanGraphSession;
import com.altamiracorp.lumify.model.TitanQueryFormatter;
import com.altamiracorp.lumify.ontology.BaseOntology;
import com.altamiracorp.lumify.search.BlurSearchProvider;
import com.altamiracorp.lumify.search.SearchProvider;


public class AppSession {
    public static final String SEARCH_PROVIDER_PROP_KEY = "search.provider";

    private static final Logger LOGGER = LoggerFactory.getLogger(AppSession.class);
    private static final String DEFAULT_SEARCH_PROVIDER = BlurSearchProvider.class.getName();
    private static Properties applicationProps = new Properties();
    private Session modelSession;
    private SearchProvider searchProvider;
    private GraphSession graphSession;

    private AppSession() {

    }

    /**
     * Store the extracted web application context properties
     *
     * @param props
     */
    public static void setApplicationProperties(final Properties props) {
        checkNotNull(props);
        applicationProps = props;
    }

    /**
     * Creates a {@link AppSession} with the extracted web context properties
     *
     * @return The created session
     */
    public static AppSession create() {
        return create(applicationProps, null);
    }

    public static AppSession create(Properties props, TaskInputOutputContext context) {
        try {
            LOGGER.info(String.format("Creating %s", AppSession.class.getSimpleName()));
            AppSession session = new AppSession();
            session.modelSession = createModelSession(props, context);
            session.searchProvider = createSearchProvider(props);
            session.graphSession = createGraphSession(props);
            return session;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static GraphSession createGraphSession(Properties props) {
        return new TitanGraphSession(props, new TitanQueryFormatter());
    }

    public static AppSession create(TaskInputOutputContext context) {
        Configuration cfg = context.getConfiguration();
        Properties properties = new Properties();
        for (Map.Entry<String, String> entry : cfg) {
            properties.setProperty(entry.getKey(), entry.getValue());
        }
        return create(properties, context);
    }

    private static SearchProvider createSearchProvider(Properties props) {
        String providerClass = props.getProperty(SEARCH_PROVIDER_PROP_KEY, DEFAULT_SEARCH_PROVIDER);

        try {
            SearchProvider provider = (SearchProvider)Class.forName(providerClass).newInstance();
            provider.setup(props);
            return provider;
        } catch (Exception e) {
            throw new RuntimeException("Failed to create search provider instance of class " + providerClass, e);
        }
    }

    private static Session createModelSession(Properties props, TaskInputOutputContext context) throws AccumuloException, AccumuloSecurityException, IOException, URISyntaxException, InterruptedException {
        String zookeeperInstanceName = props.getProperty(AccumuloSession.ZOOKEEPER_INSTANCE_NAME);
        String zookeeperServerLocations = props.getProperty(AccumuloSession.ZOOKEEPER_SERVER_NAMES);
        String username = props.getProperty(AccumuloSession.USERNAME);
        String password = props.getProperty(AccumuloSession.PASSWORD);
        ZooKeeperInstance zooKeeperInstance = new ZooKeeperInstance(zookeeperInstanceName, zookeeperServerLocations);
        Connector connector = zooKeeperInstance.getConnector(username, password);

        Configuration hadoopConfiguration = new Configuration();
        String hdfsRootDir = props.getProperty(AccumuloSession.HADOOP_URL);
        FileSystem hdfsFileSystem = FileSystem.get(new URI(hdfsRootDir), hadoopConfiguration, "hadoop");

        AccumuloQueryUser queryUser = new AccumuloQueryUser();
        return new AccumuloSession(connector, hdfsFileSystem, hdfsRootDir, queryUser, context);
    }

    public void close() {
        graphSession.close();
    }

    public Session getModelSession() {
        return modelSession;
    }

    public SearchProvider getSearchProvider() {
        return searchProvider;
    }

    public GraphSession getGraphSession() {
        return graphSession;
    }

    public void initialize() {
        getSearchProvider().initializeIndex();
        getModelSession().initializeTables();
        createBaseOntology();
    }

    private void createBaseOntology() {
        BaseOntology baseOntology = new BaseOntology();
        if (!baseOntology.isOntologyDefined(this)) {
            LOGGER.info("Base ontology not defined. Creating a new ontology.");
            baseOntology.defineOntology(this);
        } else {
            LOGGER.info("Base ontology already defined.");
        }
    }
}
