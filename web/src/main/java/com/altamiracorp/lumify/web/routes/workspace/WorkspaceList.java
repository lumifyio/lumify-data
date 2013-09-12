package com.altamiracorp.lumify.web.routes.workspace;

import com.altamiracorp.lumify.AppSession;
import com.altamiracorp.lumify.model.workspace.Workspace;
import com.altamiracorp.lumify.model.workspace.WorkspaceRepository;
import com.altamiracorp.lumify.web.BaseRequestHandler;
import com.altamiracorp.web.HandlerChain;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Collection;

public class WorkspaceList extends BaseRequestHandler {
    private WorkspaceRepository workspaceRepository = new WorkspaceRepository();

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, HandlerChain chain) throws Exception {
        AppSession session = app.getAppSession(request);

        Collection<Workspace> workspaces = workspaceRepository.findAll(session.getModelSession());

        JSONArray resultsJSON = new JSONArray();
        for (Workspace workspace : workspaces) {
            JSONObject workspaceJSON = new JSONObject();
            workspaceJSON.put("_rowKey", workspace.getRowKey());
            workspaceJSON.put("title", workspace.getMetadata().getTitle());
            resultsJSON.put(workspaceJSON);
        }

        respondWithJson(response, resultsJSON);
        chain.next(request, response);
    }
}
