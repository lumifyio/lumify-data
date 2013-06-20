package com.altamiracorp.reddawn.web.routes.workspace;

import com.altamiracorp.reddawn.RedDawnSession;
import com.altamiracorp.reddawn.model.workspace.Workspace;
import com.altamiracorp.reddawn.model.workspace.WorkspaceRepository;
import com.altamiracorp.reddawn.web.Responder;
import com.altamiracorp.reddawn.web.User;
import com.altamiracorp.reddawn.web.WebApp;
import com.altamiracorp.web.App;
import com.altamiracorp.web.AppAware;
import com.altamiracorp.web.Handler;
import com.altamiracorp.web.HandlerChain;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Collection;

public class WorkspaceList implements Handler, AppAware {
    private WorkspaceRepository workspaceRepository = new WorkspaceRepository();
    private WebApp app;

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, HandlerChain chain) throws Exception {
        RedDawnSession session = app.getRedDawnSession(request);
        User currentUser = User.getUser(request);

        Collection<Workspace> workspaces = workspaceRepository.findByUserId(session.getModelSession(), currentUser.getId());

        JSONArray resultsJSON = new JSONArray();
        for (Workspace workspace : workspaces) {
            JSONObject workspaceJSON = new JSONObject();
            workspaceJSON.put("rowKey", workspace.getRowKey());
            workspaceJSON.put("title", workspace.getMetadata().getTitle());
            resultsJSON.put(workspaceJSON);
        }

        new Responder(response).respondWith(resultsJSON);
        chain.next(request, response);
    }

    @Override
    public void setApp(App app) {
        this.app = (WebApp) app;
    }
}
