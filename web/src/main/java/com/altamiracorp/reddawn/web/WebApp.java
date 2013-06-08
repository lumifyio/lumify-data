package com.altamiracorp.reddawn.web;

import com.altamiracorp.reddawn.RedDawnSession;
import com.altamiracorp.web.App;
import org.apache.accumulo.core.client.AccumuloException;
import org.apache.accumulo.core.client.AccumuloSecurityException;

import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServletRequest;
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

    public RedDawnSession getRedDawnSession(HttpServletRequest request) throws AccumuloSecurityException, AccumuloException {
        // TODO this needs refactoring
        return WebSessionFactory.createRedDawnSession(request);
    }
}
