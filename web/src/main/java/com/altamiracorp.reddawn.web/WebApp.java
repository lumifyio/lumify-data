package com.altamiracorp.reddawn.web;

import com.altamiracorp.reddawn.web.routes.artifact.ArtifactByRowKey;
import com.altamiracorp.reddawn.web.routes.artifact.ArtifactRawByRowKey;
import com.altamiracorp.reddawn.web.routes.artifact.ArtifactTermsByRowKey;
import com.altamiracorp.reddawn.web.routes.artifact.ArtifactTextByRowKey;
import com.altamiracorp.reddawn.web.routes.search.Search;
import com.altamiracorp.reddawn.web.routes.term.TermByRowKey;
import com.altamiracorp.web.WebApp;

import javax.servlet.ServletConfig;
import java.io.File;

public class WebApp extends WebApp {
    final File rootDir = new File("./web/src/main/webapp");

    @Override
    public void setup(ServletConfig config) {
        get("/search", Search.class);

        get("/artifacts/{rowKey}/terms", ArtifactTermsByRowKey.class);
        get("/artifacts/{rowKey}/text", ArtifactTextByRowKey.class);
        get("/artifacts/{rowKey}/raw", ArtifactRawByRowKey.class);
        get("/artifacts/{rowKey}", ArtifactByRowKey.class);

        get("/terms/{rowKey}", TermByRowKey.class);

        LessRestlet.init(rootDir);
        get("/css/{file}.css", LessRestlet.class);
    }
}
