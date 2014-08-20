package io.lumify.igw;

import io.lumify.miniweb.Handler;
import io.lumify.web.WebApp;
import io.lumify.web.WebAppPlugin;

import javax.servlet.ServletContext;

public class IgwWebAppPlugin implements WebAppPlugin {
    @Override
    public void init(WebApp app, ServletContext servletContext, Handler authenticationHandler) {
        app.registerCss("/io/lumify/igw/igw-plugin.css");
        app.registerJavaScript("/io/lumify/igw/igw-plugin.js");
    }
}
