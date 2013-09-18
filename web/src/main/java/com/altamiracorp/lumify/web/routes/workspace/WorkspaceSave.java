package com.altamiracorp.lumify.web.routes.workspace;

import com.altamiracorp.lumify.core.user.User;
import com.altamiracorp.lumify.model.user.UserRepository;
import com.altamiracorp.lumify.model.workspace.Workspace;
import com.altamiracorp.lumify.model.workspace.WorkspaceRepository;
import com.altamiracorp.lumify.model.workspace.WorkspaceRowKey;
import com.altamiracorp.lumify.web.BaseRequestHandler;
import com.altamiracorp.web.HandlerChain;
import com.google.inject.Inject;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class WorkspaceSave extends BaseRequestHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(WorkspaceSave.class);
    private static final String DEFAULT_WORKSPACE_TITLE = "Default";

    private final WorkspaceRepository workspaceRepository;
    private final UserRepository userRepository;

    @Inject
    public WorkspaceSave(final WorkspaceRepository workspaceRepository, final UserRepository userRepository) {
        this.workspaceRepository = workspaceRepository;
        this.userRepository = userRepository;
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, HandlerChain chain) throws Exception {
        final String data = getOptionalParameter(request, "data");
        final String workspaceRowKeyString = getAttributeString(request, "workspaceRowKey");

        User authUser = getUser(request);
        com.altamiracorp.lumify.model.user.User user = userRepository.findOrAddUser(authUser.getUsername(), authUser);
        Workspace workspace;
        if (workspaceRowKeyString == null) {
            workspace = handleNew(request, user);
        } else {
            workspace = new Workspace(new WorkspaceRowKey(workspaceRowKeyString));
        }

        if (!workspace.getRowKey().toString().equals(user.getMetadata().getCurrentWorkspace())) {
            user.getMetadata().setCurrentWorkspace(workspace.getRowKey().toString());
            userRepository.save(user, authUser);
        }

        LOGGER.info("Saving workspace: " + workspace.getRowKey() + "\ntitle: " + workspace.getMetadata().getTitle() + "\ndata: " + data);

        if (data != null) {
            workspace.getContent().setData(data);
        }


        workspaceRepository.save(workspace, authUser);
        JSONObject resultJson = new JSONObject();
        resultJson.put("_rowKey", workspace.getRowKey().toString());
        resultJson.put("title", workspace.getMetadata().getTitle());

        respondWithJson(response, resultJson);
    }

    public Workspace handleNew(HttpServletRequest request, com.altamiracorp.lumify.model.user.User user) {
        WorkspaceRowKey workspaceRowKey = new WorkspaceRowKey(
                user.getRowKey().toString(), String.valueOf(System.currentTimeMillis()));
        Workspace workspace = new Workspace(workspaceRowKey);
        String title = getOptionalParameter(request, "title");

        if (title != null) {
            workspace.getMetadata().setTitle(title);
        } else {
            workspace.getMetadata().setTitle(DEFAULT_WORKSPACE_TITLE + " - " + user.getMetadata().getUserName());
        }

        return workspace;
    }
}
