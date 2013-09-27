package com.altamiracorp.lumify.web.routes.workspace;

import com.altamiracorp.lumify.core.user.User;
import com.altamiracorp.lumify.model.Column;
import com.altamiracorp.lumify.model.workspace.Workspace;
import com.altamiracorp.lumify.model.workspace.WorkspacePermissions;
import com.altamiracorp.lumify.model.workspace.WorkspaceRepository;
import com.altamiracorp.lumify.web.BaseRequestHandler;
import com.altamiracorp.web.HandlerChain;
import com.google.inject.Inject;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Collection;

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

        JSONArray workspacesJson = new JSONArray();
        for (Workspace workspace : workspaces) {
            JSONObject workspaceJson = new JSONObject();
            workspaceJson.put("_rowKey", workspace.getRowKey());
            workspaceJson.put("title", workspace.getMetadata().getTitle());
            workspaceJson.put("createdBy", workspace.getMetadata().getCreator());

            JSONArray permissions = new JSONArray();
            for (Column col : workspace.get(WorkspacePermissions.NAME).getColumns()) {
                JSONObject users = new JSONObject();
                users.put("user", col.getName());
                users.put("userPermissions", new JSONObject(col.getValue()));
                permissions.put(users);
            }
            workspaceJson.put("permissions", permissions);

            workspacesJson.put(workspaceJson);
        }

        JSONObject json = new JSONObject();
        json.put("workspaces", workspacesJson);

        respondWithJson(response, json);
        chain.next(request, response);
    }
}
