package com.altamiracorp.lumify.web.routes.workspace;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.altamiracorp.lumify.AppSession;
import com.altamiracorp.lumify.model.Repository;
import com.altamiracorp.lumify.model.user.User;
import com.altamiracorp.lumify.model.workspace.Workspace;
import com.altamiracorp.lumify.model.workspace.WorkspaceRowKey;
import com.altamiracorp.lumify.web.BaseRequestHandler;
import com.altamiracorp.web.HandlerChain;
import com.google.inject.Inject;

public class WorkspaceDelete extends BaseRequestHandler {
    private final Repository<Workspace> workspaceRepository;
    private static final Logger LOGGER = LoggerFactory.getLogger(WorkspaceDelete.class);

    @Inject
    public WorkspaceDelete(final Repository<Workspace> repo) {
        workspaceRepository = repo;
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, HandlerChain chain) throws Exception {
        if (isDeleteAuthorized(request)) {
            final String strRowKey = getAttributeString(request, "workspaceRowKey");

            AppSession session = app.getAppSession(request);
            WorkspaceRowKey rowKey = new WorkspaceRowKey(strRowKey);

            LOGGER.info("Deleting workspace with id: " + strRowKey);
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
