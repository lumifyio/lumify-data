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

public class WorkspaceByRowKey extends BaseRequestHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(WorkspaceByRowKey.class);
    private final WorkspaceRepository workspaceRepository;
    private final UserRepository userRepository;

    @Inject
    public WorkspaceByRowKey(final WorkspaceRepository workspaceRepo,
                             final UserRepository userRepo) {
        workspaceRepository = workspaceRepo;
        userRepository = userRepo;
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, HandlerChain chain) throws Exception {
        WorkspaceRowKey workspaceRowKey = new WorkspaceRowKey(getAttributeString(request, "workspaceRowKey"));
        User authUser = getUser(request);

        com.altamiracorp.lumify.model.user.User user = userRepository.findOrAddUser(authUser.getUsername(), authUser);
        if (!workspaceRowKey.toString().equals(user.getMetadata().getCurrentWorkspace())) {
            user.getMetadata().setCurrentWorkspace(workspaceRowKey.toString());
            authUser.setCurrentWorkspace(workspaceRowKey.toString());
            userRepository.save(user, authUser);

            LOGGER.debug("User " + user.getRowKey() + " switched current workspace to " + workspaceRowKey);
        }

        Workspace workspace = workspaceRepository.findByRowKey(workspaceRowKey.toString(), authUser);

        if (workspace == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        } else {
            JSONObject resultJSON = workspace.toJson(authUser);
            resultJSON.put("id", workspace.getRowKey().toString());

            if (workspace.getContent().getData() != null) {
                resultJSON.put("data", new JSONObject(workspace.getContent().getData()));
            }

            respondWithJson(response, resultJSON);
        }

        chain.next(request, response);
    }
}
