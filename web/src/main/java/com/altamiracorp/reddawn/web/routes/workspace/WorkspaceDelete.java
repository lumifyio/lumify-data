package com.altamiracorp.reddawn.web.routes.workspace;

import com.altamiracorp.reddawn.RedDawnSession;
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

public class WorkspaceDelete implements Handler, AppAware {
    private WorkspaceRepository workspaceRepository = new WorkspaceRepository();
    private static final Logger LOGGER = LoggerFactory.getLogger(WorkspaceDelete.class.getName());
    private WebApp app;

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, HandlerChain chain) throws Exception {
        if (isDeleteAuthorized(request)) {
            RedDawnSession session = app.getRedDawnSession(request);
            String strRowKey = (String)request.getAttribute("workspaceRowKey");
            WorkspaceRowKey rowKey = new WorkspaceRowKey(strRowKey);
            workspaceRepository.delete(session.getModelSession(), rowKey);

            JSONObject resultJson = new JSONObject();
            resultJson.put("success", true);
            new Responder(response).respondWith(resultJson);

        } else {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
        }
    }

    @Override
    public void setApp(App app) {
        this.app = (WebApp)app;
    }

    // TODO: Make this workspace delete authorization more robust
    private boolean isDeleteAuthorized(HttpServletRequest request) {
        User currentUser = User.getUser(request);
        String strRowKey = (String)request.getAttribute("workspaceRowKey");
        return strRowKey.startsWith(currentUser.getId());
    }
}
