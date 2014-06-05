package io.lumify.igw;

import com.altamiracorp.miniweb.Handler;
import io.lumify.web.WebApp;
import io.lumify.web.WebAppPlugin;

import javax.servlet.ServletConfig;

public class IgwWebAppPlugin implements WebAppPlugin {
    @Override
    public void init(WebApp app, ServletConfig config, Handler authenticationHandler) {
        app.registerCss("/io/lumify/igw/igw-plugin.css");
        app.registerJavaScript("/io/lumify/igw/igw-plugin.js");
    }
}
