package com.altamiracorp.reddawn.web;

import com.altamiracorp.reddawn.search.SearchProvider;
import com.altamiracorp.reddawn.ucd.AuthorizationLabel;
import com.altamiracorp.reddawn.ucd.UcdClient;
import org.apache.accumulo.core.client.AccumuloException;
import org.apache.accumulo.core.client.AccumuloSecurityException;

public class WebUcdClientFactory {
    private static Server ucdCommandLineBase;

    public static UcdClient<AuthorizationLabel> createUcdClient() throws AccumuloSecurityException, AccumuloException {
        // TODO refactor this
        return ucdCommandLineBase.createUcdClient();
    }

    public static void setUcdCommandLineBase(Server ucdCommandLineBase) {
        WebUcdClientFactory.ucdCommandLineBase = ucdCommandLineBase;
    }

    public static Server getUcdCommandLineBase() {
        return ucdCommandLineBase;
    }

    public static SearchProvider createSearchProvider() throws Exception {
        // TODO refactor this
        return ucdCommandLineBase.createSearchProvider();
    }
}
