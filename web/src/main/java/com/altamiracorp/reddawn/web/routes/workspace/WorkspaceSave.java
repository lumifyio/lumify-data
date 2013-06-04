package com.altamiracorp.reddawn.web.routes.workspace;

import com.altamiracorp.reddawn.RedDawnClient;
import com.altamiracorp.reddawn.model.Workspace;
import com.altamiracorp.reddawn.model.WorkspaceKey;
import com.altamiracorp.reddawn.ucd.AuthorizationLabel;
import com.altamiracorp.reddawn.ucd.QueryUser;
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
    private static final Logger LOGGER = LoggerFactory.getLogger(WorkspaceSave.class.getName());
    private WebApp app;

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, HandlerChain chain) throws Exception {
        RedDawnClient client = app.getRedDawnClient();
        QueryUser<AuthorizationLabel> queryUser = app.getQueryUser();
        User currentUser = User.getUser(request);
        String workspaceRowKeyString = (String) request.getAttribute("workspaceRowKey");
        WorkspaceKey workspaceRowKey;
        if (workspaceRowKeyString == null) {
            workspaceRowKey = new WorkspaceKey(currentUser.getId(), "1"); // TODO currently only support one workspace
        } else {
            workspaceRowKey = new WorkspaceKey(workspaceRowKeyString);
        }
        String data = request.getParameter("data");
        LOGGER.info("Saving workspace: " + workspaceRowKey + "\ndata: " + data);

        Workspace workspace = new Workspace();
        workspace.setKey(workspaceRowKey);
        workspace.setData(data);

        client.saveWorkspace(workspace, queryUser);
        JSONObject resultJson = new JSONObject();
        resultJson.put("workspaceId", workspaceRowKey.toString());

        response.setContentType("application/json");
        response.getWriter().write(resultJson.toString());
    }

    @Override
    public void setApp(App app) {
        this.app = (WebApp) app;
    }
}
