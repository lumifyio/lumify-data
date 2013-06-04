package com.altamiracorp.reddawn.web;

import com.altamiracorp.reddawn.RedDawnClient;
import com.altamiracorp.reddawn.search.SearchProvider;
import com.altamiracorp.reddawn.ucd.AuthorizationLabel;
import com.altamiracorp.reddawn.ucd.QueryUser;
import com.altamiracorp.reddawn.ucd.UcdClient;
import com.altamiracorp.web.App;
import org.apache.accumulo.core.client.AccumuloException;
import org.apache.accumulo.core.client.AccumuloSecurityException;

import javax.servlet.ServletConfig;
import java.util.Enumeration;


public class WebApp extends App {
    public WebApp(ServletConfig servletConfig) {
        super(servletConfig);

        Enumeration initParamNames = servletConfig.getInitParameterNames();
        while (initParamNames.hasMoreElements()) {
            String initParam = (String) initParamNames.nextElement();
            set(initParam, servletConfig.getInitParameter(initParam));
        }
    }

    public UcdClient<AuthorizationLabel> getUcdClient() throws AccumuloSecurityException, AccumuloException {
        // TODO this needs refactoring
        return WebUcdClientFactory.createUcdClient();
    }

    public RedDawnClient getRedDawnClient() throws AccumuloSecurityException, AccumuloException {
        // TODO this needs refactoring
        return WebUcdClientFactory.createRedDawnClient();
    }

    public QueryUser<AuthorizationLabel> getQueryUser() {
        // TODO this needs configuring
        return new QueryUser<AuthorizationLabel>("U", new AuthorizationLabel());
    }

    public SearchProvider getSearchProvider() throws Exception {
        // TODO this needs configuring
        return WebUcdClientFactory.createSearchProvider();
    }
}
