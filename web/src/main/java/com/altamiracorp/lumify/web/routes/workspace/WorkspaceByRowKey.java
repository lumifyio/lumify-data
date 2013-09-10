package com.altamiracorp.lumify.web.routes.workspace;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONObject;

import com.altamiracorp.lumify.AppSession;
import com.altamiracorp.lumify.model.user.User;
import com.altamiracorp.lumify.model.user.UserRepository;
import com.altamiracorp.lumify.model.workspace.Workspace;
import com.altamiracorp.lumify.model.workspace.WorkspaceRepository;
import com.altamiracorp.lumify.model.workspace.WorkspaceRowKey;
import com.altamiracorp.lumify.web.BaseRequestHandler;
import com.altamiracorp.lumify.web.DevBasicAuthenticator;
import com.altamiracorp.web.HandlerChain;

public class WorkspaceByRowKey extends BaseRequestHandler {
    private WorkspaceRepository workspaceRepository = new WorkspaceRepository();
    private UserRepository userRepository = new UserRepository();

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, HandlerChain chain) throws Exception {
        WorkspaceRowKey workspaceRowKey = new WorkspaceRowKey(getAttributeString(request, "workspaceRowKey"));
        AppSession session = app.getAppSession(request);

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

            respondWithJson(response, resultJSON);
        }

        chain.next(request, response);
    }
}
