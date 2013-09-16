package com.altamiracorp.lumify.web.routes.workspace;

import com.altamiracorp.lumify.AppSession;
import com.altamiracorp.lumify.model.user.User;
import com.altamiracorp.lumify.model.workspace.WorkspaceRepository;
import com.altamiracorp.lumify.model.workspace.WorkspaceRowKey;
import com.altamiracorp.lumify.web.BaseRequestHandler;
import com.altamiracorp.web.HandlerChain;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class WorkspaceDelete extends BaseRequestHandler {
    private WorkspaceRepository workspaceRepository = new WorkspaceRepository();
    private static final Logger LOGGER = LoggerFactory.getLogger(WorkspaceDelete.class);

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, HandlerChain chain) throws Exception {
        if (isDeleteAuthorized(request)) {
            final String strRowKey = getAttributeString(request, "workspaceRowKey");

            AppSession session = app.getAppSession(request);
            WorkspaceRowKey rowKey = new WorkspaceRowKey(strRowKey);
            workspaceRepository.delete(session.getModelSession(), rowKey);

            JSONObject resultJson = new JSONObject();
            resultJson.put("success", true);

            respondWithJson(response, resultJson);

        } else {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
        }
    }

    // TODO: Make this workspace delete authorization more robust
    private boolean isDeleteAuthorized(HttpServletRequest request) {
        User currentUser = getUser(request);
        final String strRowKey = getAttributeString(request, "workspaceRowKey");
        return strRowKey.startsWith(currentUser.getRowKey().toString());
    }
}
