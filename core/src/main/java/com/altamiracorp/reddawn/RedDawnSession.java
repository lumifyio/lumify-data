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
        } catch (AccumuloSecurityException e) {
            throw new RuntimeException(e);
        } catch (AccumuloException e) {
            throw new RuntimeException(e);
        }
    }

    private static SearchProvider createSearchProvider(Properties props) {
        BlurSearchProvider blurSearchProvider = new BlurSearchProvider();
        blurSearchProvider.setup(props);
        return blurSearchProvider;
    }

    private static Session createModelSession(Properties props) throws AccumuloException, AccumuloSecurityException {
        String zookeeperInstanceName = props.getProperty(AccumuloSession.ZOOKEEPER_INSTANCE_NAME);
        String zookeeperServerName = props.getProperty(AccumuloSession.ZOOKEEPER_SERVER_NAMES);
        String username = props.getProperty(AccumuloSession.USERNAME);
        String password = props.getProperty(AccumuloSession.PASSWORD);
        ZooKeeperInstance zooKeeperInstance = new ZooKeeperInstance(zookeeperInstanceName, zookeeperServerName);
        Connector connector = zooKeeperInstance.getConnector(username, password);

        AccumuloQueryUser queryUser = new AccumuloQueryUser();
        return new AccumuloSession(connector, queryUser);
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
