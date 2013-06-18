package com.altamiracorp.reddawn.web.routes.workspace;

import com.altamiracorp.reddawn.RedDawnSession;
import com.altamiracorp.reddawn.model.workspace.Workspace;
import com.altamiracorp.reddawn.model.workspace.WorkspaceRepository;
import com.altamiracorp.reddawn.model.workspace.WorkspaceRowKey;
import com.altamiracorp.reddawn.web.Responder;
import com.altamiracorp.reddawn.web.User;
import com.altamiracorp.reddawn.web.WebApp;
import com.altamiracorp.web.App;
import com.altamiracorp.web.AppAware;
import com.altamiracorp.web.Handler;
import com.altamiracorp.web.HandlerChain;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class WorkspaceSave implements Handler, AppAware {
    private WorkspaceRepository workspaceRepository = new WorkspaceRepository();
    private static final Logger LOGGER = LoggerFactory.getLogger(WorkspaceSave.class.getName());
    private WebApp app;

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, HandlerChain chain) throws Exception {
        RedDawnSession session = app.getRedDawnSession(request);
        User currentUser = User.getUser(request);
        String workspaceRowKeyString = (String) request.getAttribute("workspaceRowKey");
        WorkspaceRowKey workspaceRowKey;
        if (workspaceRowKeyString == null) {
            workspaceRowKey = new WorkspaceRowKey(currentUser.getId(), "1"); // TODO currently only support one workspace
        } else {
            workspaceRowKey = new WorkspaceRowKey(workspaceRowKeyString);
        }
        String data = request.getParameter("data");
        LOGGER.info("Saving workspace: " + workspaceRowKey + "\ndata: " + data);

        Workspace workspace = new Workspace(workspaceRowKey);
        workspace.getContent().setData(data);

        workspaceRepository.save(session.getModelSession(), workspace);
        JSONObject resultJson = new JSONObject();
        resultJson.put("workspaceId", workspaceRowKey.toString());

        new Responder(response).respondWith(resultJson);
    }

    @Override
    public void setApp(App app) {
        this.app = (WebApp) app;
    }
}
