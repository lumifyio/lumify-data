package com.altamiracorp.reddawn.web.routes.workspace;

import com.altamiracorp.reddawn.RedDawnClient;
import com.altamiracorp.reddawn.model.Workspace;
import com.altamiracorp.reddawn.model.WorkspaceKey;
import com.altamiracorp.reddawn.ucd.AuthorizationLabel;
import com.altamiracorp.reddawn.ucd.QueryUser;
import com.altamiracorp.reddawn.web.WebApp;
import com.altamiracorp.web.App;
import com.altamiracorp.web.AppAware;
import com.altamiracorp.web.Handler;
import com.altamiracorp.web.HandlerChain;
import org.json.JSONObject;
import org.mortbay.util.ajax.JSON;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class WorkspaceByRowKey implements Handler, AppAware {
    private WebApp app;

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, HandlerChain chain) throws Exception {
        RedDawnClient client = app.getRedDawnClient();
        QueryUser<AuthorizationLabel> queryUser = app.getQueryUser();
        WorkspaceKey workspaceRowKey = new WorkspaceKey((String) request.getAttribute("workspaceRowKey"));

        Workspace workspace = client.queryWorkspaceByRowKey(workspaceRowKey, queryUser);

        if (workspace == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        } else {
            response.setContentType("application/json");
            JSONObject resultJSON = new JSONObject();
            resultJSON.put("id", workspace.getKey().toString());
            resultJSON.put("data", new JSONObject(workspace.getData()));
            response.getWriter().write(resultJSON.toString());
        }

        chain.next(request, response);
    }

    @Override
    public void setApp(App app) {
        this.app = (WebApp) app;
    }
}
