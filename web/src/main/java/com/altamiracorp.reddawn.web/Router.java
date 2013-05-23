package com.altamiracorp.reddawn.web;

import com.altamiracorp.reddawn.web.routes.artifact.ArtifactByRowKey;
import com.altamiracorp.reddawn.web.routes.artifact.ArtifactRawByRowKey;
import com.altamiracorp.reddawn.web.routes.artifact.ArtifactTermsByRowKey;
import com.altamiracorp.reddawn.web.routes.artifact.ArtifactTextByRowKey;
import com.altamiracorp.reddawn.web.routes.search.Search;
import com.altamiracorp.reddawn.web.routes.term.TermByRowKey;
import com.altamiracorp.web.App;

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

        app.get("/search", Search.class);

        app.get("/artifacts/{rowKey}/terms", ArtifactTermsByRowKey.class);
        app.get("/artifacts/{rowKey}/text", ArtifactTextByRowKey.class);
        app.get("/artifacts/{rowKey}/raw", ArtifactRawByRowKey.class);
        app.get("/artifacts/{rowKey}", ArtifactByRowKey.class);

        app.get("/terms/{rowKey}", TermByRowKey.class);

        LessRestlet.init(rootDir);
        app.get("/css/{file}.css", LessRestlet.class);
    }

    @Override
    public void service(ServletRequest req, ServletResponse resp) throws ServletException, IOException {
        try {
            app.handle((HttpServletRequest)req, (HttpServletResponse)resp);
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }
}
