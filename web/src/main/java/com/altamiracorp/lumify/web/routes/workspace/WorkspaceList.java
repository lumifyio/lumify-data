package com.altamiracorp.lumify.web.routes.workspace;

import java.util.Collection;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONObject;

import com.altamiracorp.lumify.AppSession;
import com.altamiracorp.lumify.model.Repository;
import com.altamiracorp.lumify.model.workspace.Workspace;
import com.altamiracorp.lumify.web.BaseRequestHandler;
import com.altamiracorp.web.HandlerChain;
import com.google.inject.Inject;

public class WorkspaceList extends BaseRequestHandler {
    private final Repository<Workspace> workspaceRepository;

    @Inject
    public WorkspaceList(final Repository<Workspace> repo) {
        workspaceRepository = repo;
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, HandlerChain chain) throws Exception {
        AppSession session = app.getAppSession(request);

        Collection<Workspace> workspaces = workspaceRepository.findAll(session.getModelSession());

        JSONArray workspacesJson = new JSONArray();
        for (Workspace workspace : workspaces) {
            JSONObject workspaceJson = new JSONObject();
            workspaceJson.put("_rowKey", workspace.getRowKey());
            workspaceJson.put("title", workspace.getMetadata().getTitle());
            workspacesJson.put(workspaceJson);
        }

        JSONObject json = new JSONObject();
        json.put("workspaces", workspacesJson);

        respondWithJson(response, json);
        chain.next(request, response);
    }
}
