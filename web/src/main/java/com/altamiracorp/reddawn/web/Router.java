package com.altamiracorp.reddawn.web;

import com.altamiracorp.reddawn.web.routes.artifact.*;
import com.altamiracorp.reddawn.web.routes.chat.ChatNew;
import com.altamiracorp.reddawn.web.routes.chat.ChatPostMessage;
import com.altamiracorp.reddawn.web.routes.concept.ConceptList;
import com.altamiracorp.reddawn.web.routes.entity.*;
import com.altamiracorp.reddawn.web.routes.map.MapInitHandler;
import com.altamiracorp.reddawn.web.routes.map.MapTileHandler;
import com.altamiracorp.reddawn.web.routes.statement.StatementByRowKey;
import com.altamiracorp.reddawn.web.routes.user.MeGet;
import com.altamiracorp.reddawn.web.routes.user.MessagesGet;
import com.altamiracorp.reddawn.web.routes.workspace.WorkspaceByRowKey;
import com.altamiracorp.reddawn.web.routes.workspace.WorkspaceDelete;
import com.altamiracorp.reddawn.web.routes.workspace.WorkspaceList;
import com.altamiracorp.reddawn.web.routes.workspace.WorkspaceSave;
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

        app.get("/concept/", ConceptList.class);

        app.get("/artifact/search", authenticator, ArtifactSearch.class);
        app.get("/artifact/{rowKey}/terms", authenticator, ArtifactTermsByRowKey.class);
        app.get("/artifact/{rowKey}/text", authenticator, ArtifactTextByRowKey.class);
        app.get("/artifact/{rowKey}/raw", authenticator, ArtifactRawByRowKey.class);
        app.get("/artifact/{rowKey}/poster-frame", authenticator, ArtifactPosterFrameByRowKey.class);
        app.get("/artifact/{rowKey}/video-preview", authenticator, ArtifactVideoPreviewImageByRowKey.class);
        app.get("/artifact/{rowKey}/relatedEntities", authenticator, ArtifactByRelatedEntities.class);
        app.get("/artifact/{rowKey}", authenticator, ArtifactByRowKey.class);

        app.get("/statement/{rowKey}", authenticator, StatementByRowKey.class);

        app.get("/entity/relationship", authenticator, EntityToEntityRelationship.class);
        app.post("/entity/relationships", authenticator, EntityRelationships.class);
        app.get("/entity/search", authenticator, EntitySearch.class);
        app.get("/entity/{rowKey}/mentions", authenticator, EntityMentionsByRange.class);
        app.get("/entity/{rowKey}/relationships", authenticator, EntityRelationshipsBidrectional.class);
        app.get("/entity/{rowKey}/relatedEntities", authenticator, EntityByRelatedEntities.class);
        app.get("/entity/{rowKey}", authenticator, EntityByRowKey.class);
        app.post("/entity/create", authenticator, EntityCreate.class);

        app.get("/workspace/", authenticator, WorkspaceList.class);
        app.post("/workspace/save", authenticator, WorkspaceSave.class);
        app.post("/workspace/{workspaceRowKey}/save", authenticator, WorkspaceSave.class);
        app.get("/workspace/{workspaceRowKey}", authenticator, WorkspaceByRowKey.class);
        app.delete("/workspace/{workspaceRowKey}", authenticator, WorkspaceDelete.class);

        app.get("/user/messages", authenticator, MessagesGet.class);
        app.get("/user/me", authenticator, MeGet.class);

        app.get("/map/map-init.js", MapInitHandler.class);
        app.get("/map/{z}/{x}/{y}.png", MapTileHandler.class);

        app.post("/chat/new", authenticator, ChatNew.class);
        app.post("/chat/{chatId}/post", authenticator, ChatPostMessage.class);

        LessRestlet.init(rootDir);
        app.get("/css/{file}.css", LessRestlet.class);
    }

    @Override
    public void service(ServletRequest req, ServletResponse resp) throws ServletException, IOException {
        try {
            HttpServletResponse httpResponse = (HttpServletResponse) resp;
            httpResponse.addHeader("Accept-Ranges", "bytes");
            app.handle((HttpServletRequest) req, httpResponse);
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }
}
