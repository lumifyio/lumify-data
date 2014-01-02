package com.altamiracorp.lumify.web.routes.workspace;

import com.altamiracorp.lumify.core.model.user.UserRepository;
import com.altamiracorp.lumify.core.model.user.UserRow;
import com.altamiracorp.lumify.core.model.workspace.Workspace;
import com.altamiracorp.lumify.core.model.workspace.WorkspaceRepository;
import com.altamiracorp.lumify.core.model.workspace.WorkspaceRowKey;
import com.altamiracorp.lumify.core.user.User;
import com.altamiracorp.lumify.web.BaseRequestHandler;
import com.altamiracorp.miniweb.HandlerChain;
import com.google.inject.Inject;
import org.json.JSONObject;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class WorkspaceCopy extends BaseRequestHandler {
    private final WorkspaceRepository workspaceRepository;
    private final UserRepository userRepository;

    @Inject
    public WorkspaceCopy(final WorkspaceRepository workspaceRepository, final UserRepository userRepository) {
        this.workspaceRepository = workspaceRepository;
        this.userRepository = userRepository;
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, HandlerChain chain) throws Exception {
        final String originalRowKey = getAttributeString(request, "workspaceRowKey");

        User authUser = getUser(request);
        UserRow user = userRepository.findOrAddUser(authUser.getUsername(), authUser);
        Workspace originalWorkspace = workspaceRepository.findByRowKey(originalRowKey, authUser.getModelUserContext());
        Workspace workspace = createNewWorkspace(originalWorkspace.getMetadata().getTitle(), user);

        if (originalWorkspace.getContent().getData() != null) {
            workspace.getContent().setData(originalWorkspace.getContent().getData());
        }

        workspaceRepository.save(workspace, authUser.getModelUserContext());
        request.getSession().setAttribute("activeWorkspace", workspace.getRowKey().toString());

        JSONObject resultJson = workspace.toJson(authUser);

        respondWithJson(response, resultJson);
    }

    public Workspace createNewWorkspace(String title, UserRow user) {
        WorkspaceRowKey workspaceRowKey = new WorkspaceRowKey(
                user.getRowKey().toString(), String.valueOf(System.currentTimeMillis()));
        Workspace workspace = new Workspace(workspaceRowKey);

        workspace.getMetadata().setTitle("Copy of " + title);
        workspace.getMetadata().setCreator(user.getRowKey().toString());

        return workspace;
    }
}
