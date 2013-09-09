package com.altamiracorp.lumify.web.routes.workspace;

import com.altamiracorp.lumify.AppSession;
import com.altamiracorp.lumify.model.user.User;
import com.altamiracorp.lumify.model.user.UserRepository;
import com.altamiracorp.lumify.model.workspace.Workspace;
import com.altamiracorp.lumify.model.workspace.WorkspaceRepository;
import com.altamiracorp.lumify.model.workspace.WorkspaceRowKey;
import com.altamiracorp.lumify.web.DevBasicAuthenticator;
import com.altamiracorp.lumify.web.Responder;
import com.altamiracorp.lumify.web.WebApp;
import com.altamiracorp.web.App;
import com.altamiracorp.web.AppAware;
import com.altamiracorp.web.Handler;
import com.altamiracorp.web.HandlerChain;
import org.json.JSONObject;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class WorkspaceByRowKey implements Handler, AppAware {
    private WorkspaceRepository workspaceRepository = new WorkspaceRepository();
    private UserRepository userRepository = new UserRepository();
    private WebApp app;

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, HandlerChain chain) throws Exception {
        AppSession session = app.getAppSession(request);
        WorkspaceRowKey workspaceRowKey = new WorkspaceRowKey((String) request.getAttribute("workspaceRowKey"));

        User currentUser = DevBasicAuthenticator.getUser(request);
        if (!workspaceRowKey.toString().equals(currentUser.getMetadata().getCurrentWorkspace())) {
            currentUser.getMetadata().setCurrentWorkspace(workspaceRowKey.toString());
            userRepository.save(session.getModelSession(), currentUser);
        }

        Workspace workspace = workspaceRepository.findByRowKey(session.getModelSession(), workspaceRowKey.toString());

        if (workspace == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        } else {
            JSONObject resultJSON = new JSONObject();
            resultJSON.put("id", workspace.getRowKey().toString());

            if (workspace.getContent().getData() != null) {
                resultJSON.put("data", new JSONObject(workspace.getContent().getData()));
            }

            new Responder(response).respondWith(resultJSON);
        }

        chain.next(request, response);
    }

    @Override
    public void setApp(App app) {
        this.app = (WebApp) app;
    }
}
