package com.altamiracorp.lumify.web.routes.workspace;

import java.util.Collection;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONObject;

import com.altamiracorp.lumify.core.user.User;
import com.altamiracorp.lumify.model.workspace.Workspace;
import com.altamiracorp.lumify.model.workspace.WorkspaceRepository;
import com.altamiracorp.lumify.web.BaseRequestHandler;
import com.altamiracorp.web.HandlerChain;
import com.google.inject.Inject;

public class WorkspaceList extends BaseRequestHandler {
    private final WorkspaceRepository workspaceRepository;

    @Inject
    public WorkspaceList(final WorkspaceRepository workspaceRepository) {
        this.workspaceRepository = workspaceRepository;
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, HandlerChain chain) throws Exception {
        User user = getUser(request);

        Collection<Workspace> workspaces = workspaceRepository.findAll(user);
        String activeWorkspaceRowKey = (String)request.getSession().getAttribute("activeWorkspace");
        activeWorkspaceRowKey = activeWorkspaceRowKey != null ? activeWorkspaceRowKey : "";
        JSONArray workspacesJson = new JSONArray();
        for (Workspace workspace : workspaces) {
            JSONObject workspaceJson = workspace.toJson(user);
            if (workspaceJson != null) {
                if (activeWorkspaceRowKey.equals(workspace.getRowKey().toString())) { //if its the active one
                    workspaceJson.put("active", true);
                }
                workspacesJson.put(workspaceJson);
            }
        }

        JSONObject json = new JSONObject();
        json.put("workspaces", workspacesJson);

        respondWithJson(response, json);
        chain.next(request, response);
    }
}
