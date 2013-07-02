package com.altamiracorp.reddawn;

import com.altamiracorp.reddawn.model.AccumuloQueryUser;
import com.altamiracorp.reddawn.model.AccumuloSession;
import com.altamiracorp.reddawn.model.Session;
import com.altamiracorp.reddawn.search.BlurSearchProvider;
import com.altamiracorp.reddawn.search.SearchProvider;
import org.apache.accumulo.core.client.AccumuloException;
import org.apache.accumulo.core.client.AccumuloSecurityException;
import org.apache.accumulo.core.client.Connector;
import org.apache.accumulo.core.client.ZooKeeperInstance;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.hdfs.DistributedFileSystem;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

public class RedDawnSession {
    private Session modelSession;
    private SearchProvider searchProvider;

    private RedDawnSession() {

    }

    public static RedDawnSession create(Properties props) {
        try {
            RedDawnSession session = new RedDawnSession();
            session.modelSession = createModelSession(props);
            session.searchProvider = createSearchProvider(props);
            return session;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static SearchProvider createSearchProvider(Properties props) {
        BlurSearchProvider blurSearchProvider = new BlurSearchProvider();
        blurSearchProvider.setup(props);
        return blurSearchProvider;
    }

    private static Session createModelSession(Properties props) throws AccumuloException, AccumuloSecurityException, IOException, URISyntaxException, InterruptedException {
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
        return new AccumuloSession(connector, hdfsFileSystem, hdfsRootDir, queryUser);
    }

    public void close() {

    }

    public Session getModelSession() {
        return this.modelSession;
    }

    public SearchProvider getSearchProvider() {
        return this.searchProvider;
    }
}
