package com.altamiracorp.lumify.web;

import com.altamiracorp.lumify.AppSession;
import com.altamiracorp.web.App;
import com.google.inject.Injector;
import org.apache.accumulo.core.client.AccumuloException;
import org.apache.accumulo.core.client.AccumuloSecurityException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import java.util.Enumeration;


public class WebApp extends App {
    private static final String APP_SESSION = "appSession";

    public WebApp(final ServletConfig servletConfig, final Injector injector) {
        super(servletConfig, injector);

        Enumeration initParamNames = servletConfig.getInitParameterNames();
        while (initParamNames.hasMoreElements()) {
            String initParam = (String) initParamNames.nextElement();
            set(initParam, servletConfig.getInitParameter(initParam));
        }
    }

    public AppSession getAppSession(HttpServletRequest request) throws AccumuloSecurityException, AccumuloException {
        // TODO this needs refactoring
        AppSession session = WebSessionFactory.createAppSession(request);
        request.setAttribute(APP_SESSION, session);
        return session;
    }

    public void close(ServletRequest request) {
        // TODO session is held open by WebSessionFactory so we don't close it
//        AppSession session = (AppSession) request.getAttribute(APP_SESSION);
//        if (session != null) {
//            session.close();
//        }
    }
}
