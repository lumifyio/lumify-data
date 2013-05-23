package com.altamiracorp.reddawn.web;

import com.altamiracorp.reddawn.ucd.AuthorizationLabel;
import com.altamiracorp.reddawn.ucd.QueryUser;
import com.altamiracorp.reddawn.ucd.UcdClient;
import com.altamiracorp.web.App;
import org.apache.accumulo.core.client.AccumuloException;
import org.apache.accumulo.core.client.AccumuloSecurityException;

import javax.servlet.ServletConfig;


public class WebApp extends App {
    public WebApp(ServletConfig servletConfig) {
        super(servletConfig);
    }

    public UcdClient<AuthorizationLabel> getUcdClient() throws AccumuloSecurityException, AccumuloException {
        // TODO this needs refactoring
        return WebUcdClientFactory.createUcdClient();
    }

    public QueryUser<AuthorizationLabel> getQueryUser() {
        // TODO this needs configuring
        return new QueryUser<AuthorizationLabel>("U", new AuthorizationLabel());
    }
}
