package com.altamiracorp.reddawn;

import com.altamiracorp.reddawn.model.*;
import com.altamiracorp.reddawn.search.BlurSearchProvider;
import com.altamiracorp.reddawn.search.SearchProvider;
import org.apache.accumulo.core.client.AccumuloException;
import org.apache.accumulo.core.client.AccumuloSecurityException;
import org.apache.accumulo.core.client.Connector;
import org.apache.accumulo.core.client.ZooKeeperInstance;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.hdfs.DistributedFileSystem;
import org.apache.hadoop.mapreduce.TaskInputOutputContext;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.Properties;

public class RedDawnSession {
    private Session modelSession;
    private SearchProvider searchProvider;
    private GraphSession graphSession;

    private RedDawnSession() {

    }

    public static RedDawnSession create(Properties props, TaskInputOutputContext context) {
        try {
            RedDawnSession session = new RedDawnSession();
            session.modelSession = createModelSession(props, context);
            session.searchProvider = createSearchProvider(props);
            session.graphSession = createGraphSession(props);
            return session;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static GraphSession createGraphSession(Properties props) {
        return new TitanGraphSession(props);
    }

    public static RedDawnSession create(TaskInputOutputContext context) {
        Configuration cfg = context.getConfiguration();
        Properties properties = new Properties();
        for (Map.Entry<String, String> entry : cfg) {
            properties.setProperty(entry.getKey(), entry.getValue());
        }
        return create(properties, context);
    }

    private static SearchProvider createSearchProvider(Properties props) {
        BlurSearchProvider blurSearchProvider = new BlurSearchProvider();
        blurSearchProvider.setup(props);
        return blurSearchProvider;
    }

    private static Session createModelSession(Properties props, TaskInputOutputContext context) throws AccumuloException, AccumuloSecurityException, IOException, URISyntaxException, InterruptedException {
        String zookeeperInstanceName = props.getProperty(AccumuloSession.ZOOKEEPER_INSTANCE_NAME);
        String zookeeperServerName = props.getProperty(AccumuloSession.ZOOKEEPER_SERVER_NAMES);
        String username = props.getProperty(AccumuloSession.USERNAME);
        String password = props.getProperty(AccumuloSession.PASSWORD);
        ZooKeeperInstance zooKeeperInstance = new ZooKeeperInstance(zookeeperInstanceName, zookeeperServerName);
        Connector connector = zooKeeperInstance.getConnector(username, password);

        Configuration hadoopConfiguration = new Configuration();
        String hdfsRootDir = props.getProperty(AccumuloSession.HADOOP_URL);
        FileSystem hdfsFileSystem = DistributedFileSystem.get(new URI(hdfsRootDir), hadoopConfiguration, "hadoop");

        AccumuloQueryUser queryUser = new AccumuloQueryUser();
        return new AccumuloSession(connector, hdfsFileSystem, hdfsRootDir, queryUser, context);
    }

    public void close() {
        graphSession.close();
    }

    public Session getModelSession() {
        return this.modelSession;
    }

    public SearchProvider getSearchProvider() {
        return this.searchProvider;
    }

    public GraphSession getGraphSession() {
        return graphSession;
    }
}
