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

public class WorkspaceCopy extends BaseRequestHandler {
    private final WorkspaceRepository workspaceRepository;
    private final UserRepository userRepository;
    private static final Logger LOGGER = LoggerFactory.getLogger(WorkspaceCopy.class);

    @Inject
    public WorkspaceCopy(final WorkspaceRepository workspaceRepository,final UserRepository userRepository) {
        this.workspaceRepository = workspaceRepository;
        this.userRepository = userRepository;
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, HandlerChain chain) throws Exception {
        final String originalRowKey = getAttributeString(request, "workspaceRowKey");

        User authUser = getUser(request);
        com.altamiracorp.lumify.model.user.User user = userRepository.findOrAddUser(authUser.getUsername(), authUser);
        Workspace originalWorkspace = workspaceRepository.findByRowKey(originalRowKey,authUser);
        Workspace workspace = createNewWorkspace(originalWorkspace.getMetadata().getTitle(),user);

        if (originalWorkspace.getContent().getData() != null) {
            workspace.getContent().setData(originalWorkspace.getContent().getData());
        }

        workspaceRepository.save(workspace,authUser);
        request.getSession().setAttribute("activeWorkspace", workspace.getRowKey().toString());


        JSONObject resultJson = workspace.toJson(authUser);

        respondWithJson(response, resultJson);
    }

    public Workspace createNewWorkspace(String title, com.altamiracorp.lumify.model.user.User user) {
        WorkspaceRowKey workspaceRowKey = new WorkspaceRowKey(
                user.getRowKey().toString(), String.valueOf(System.currentTimeMillis()));
        Workspace workspace = new Workspace(workspaceRowKey);

        workspace.getMetadata().setTitle("Copy of " + title);
        workspace.getMetadata().setCreator(user.getRowKey().toString());

        return workspace;
    }
}
