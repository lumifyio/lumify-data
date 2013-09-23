package com.altamiracorp.lumify.web;

import com.altamiracorp.web.App;
import com.google.inject.Injector;

import javax.servlet.ServletConfig;
import java.util.Enumeration;


public class WebApp extends App {
    public WebApp(final ServletConfig servletConfig, final Injector injector) {
        super(servletConfig, injector);

        Enumeration initParamNames = servletConfig.getInitParameterNames();
        while (initParamNames.hasMoreElements()) {
            String initParam = (String) initParamNames.nextElement();
            set(initParam, servletConfig.getInitParameter(initParam));
        }
    }
}
