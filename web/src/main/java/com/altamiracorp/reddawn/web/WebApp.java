package com.altamiracorp.reddawn.web;

import com.altamiracorp.reddawn.RedDawnSession;
import com.altamiracorp.web.App;
import com.google.inject.Injector;
import org.apache.accumulo.core.client.AccumuloException;
import org.apache.accumulo.core.client.AccumuloSecurityException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import java.util.Enumeration;


public class WebApp extends App {
    private static final String RED_DAWN_SESSION = "redDawnSession";

    public WebApp(final ServletConfig servletConfig, final Injector injector) {
        super(servletConfig, injector);

        Enumeration initParamNames = servletConfig.getInitParameterNames();
        while (initParamNames.hasMoreElements()) {
            String initParam = (String) initParamNames.nextElement();
            set(initParam, servletConfig.getInitParameter(initParam));
        }
    }

    public RedDawnSession getRedDawnSession(HttpServletRequest request) throws AccumuloSecurityException, AccumuloException {
        // TODO this needs refactoring
        RedDawnSession session = WebSessionFactory.createRedDawnSession(request);
        request.setAttribute(RED_DAWN_SESSION, session);
        return session;
    }

    public void close(ServletRequest request) {
        // TODO session is held open by WebSessionFactory so we don't close it
//        RedDawnSession session = (RedDawnSession) request.getAttribute(RED_DAWN_SESSION);
//        if (session != null) {
//            session.close();
//        }
    }
}
