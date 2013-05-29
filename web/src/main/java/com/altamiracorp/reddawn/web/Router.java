package com.altamiracorp.reddawn.web;

import com.altamiracorp.reddawn.web.routes.artifact.*;
import com.altamiracorp.reddawn.web.routes.entity.EntityByRowKey;
import com.altamiracorp.reddawn.web.routes.entity.EntitySearch;
import com.altamiracorp.web.App;
import com.altamiracorp.web.Handler;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;

public class Router extends HttpServlet {
    private App app;
    final File rootDir = new File("./web/src/main/webapp");

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        app = new WebApp(config);

        Class<? extends Handler> authenticator = X509Authenticator.class;
        if (app.get("env").equals("dev")) {
            authenticator = DevBasicAuthenticator.class;
        }

        app.get("/artifact/search", authenticator, ArtifactSearch.class);
        app.get("/artifact/{rowKey}/terms", authenticator, ArtifactTermsByRowKey.class);
        app.get("/artifact/{rowKey}/text", authenticator, ArtifactTextByRowKey.class);
        app.get("/artifact/{rowKey}/raw", authenticator, ArtifactRawByRowKey.class);
        app.get("/artifact/{rowKey}", authenticator, ArtifactByRowKey.class);

        app.get("/entity/search", authenticator, EntitySearch.class);
        app.get("/entity/{rowKey}", authenticator, EntityByRowKey.class);

        LessRestlet.init(rootDir);
        app.get("/css/{file}.css", LessRestlet.class);
    }

    @Override
    public void service(ServletRequest req, ServletResponse resp) throws ServletException, IOException {
        try {
            app.handle((HttpServletRequest) req, (HttpServletResponse) resp);
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }
}
