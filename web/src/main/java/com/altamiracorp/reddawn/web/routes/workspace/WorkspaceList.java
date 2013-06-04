package com.altamiracorp.reddawn.web.routes.workspace;

import com.altamiracorp.reddawn.RedDawnClient;
import com.altamiracorp.reddawn.model.Workspace;
import com.altamiracorp.reddawn.ucd.AuthorizationLabel;
import com.altamiracorp.reddawn.ucd.QueryUser;
import com.altamiracorp.reddawn.web.User;
import com.altamiracorp.reddawn.web.WebApp;
import com.altamiracorp.web.App;
import com.altamiracorp.web.AppAware;
import com.altamiracorp.web.Handler;
import com.altamiracorp.web.HandlerChain;
import org.json.JSONArray;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Collection;

public class WorkspaceList implements Handler, AppAware {
    private WebApp app;

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, HandlerChain chain) throws Exception {
        RedDawnClient client = app.getRedDawnClient();
        User currentUser = User.getUser(request);
        QueryUser<AuthorizationLabel> queryUser = app.getQueryUser();

        Collection<Workspace> workspaces = client.queryWorkspaceByUserId(currentUser.getId(), queryUser);

        JSONArray resultsJSON = new JSONArray();
        for (Workspace workspace : workspaces) {
            resultsJSON.put(workspace.getKey().toString());
        }

        response.setContentType("application/json");
        response.getWriter().write(resultsJSON.toString());
        chain.next(request, response);
    }

    @Override
    public void setApp(App app) {
        this.app = (WebApp) app;
    }
}
