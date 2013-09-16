package com.altamiracorp.lumify.web.routes.workspace;

import com.altamiracorp.lumify.AppSession;
import com.altamiracorp.lumify.model.user.User;
import com.altamiracorp.lumify.model.user.UserRepository;
import com.altamiracorp.lumify.model.workspace.Workspace;
import com.altamiracorp.lumify.model.workspace.WorkspaceRepository;
import com.altamiracorp.lumify.model.workspace.WorkspaceRowKey;
import com.altamiracorp.lumify.web.BaseRequestHandler;
import com.altamiracorp.web.HandlerChain;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class WorkspaceSave extends BaseRequestHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(WorkspaceSave.class.getName());
    private static final String DEFAULT_WORKSPACE_TITLE = "Default";
    private WorkspaceRepository workspaceRepository = new WorkspaceRepository();
    private UserRepository userRepository = new UserRepository();

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, HandlerChain chain) throws Exception {
        final String data = getOptionalParameter(request, "data");
        final String workspaceRowKeyString = getAttributeString(request, "workspaceRowKey");

        AppSession session = app.getAppSession(request);
        Workspace workspace;
        if (workspaceRowKeyString == null) {
            workspace = handleNew(request);
        } else {
            workspace = new Workspace(new WorkspaceRowKey(workspaceRowKeyString));
        }

        User currentUser = getUser(request);
        if (!workspace.getRowKey().toString().equals(currentUser.getMetadata().getCurrentWorkspace())) {
            currentUser.getMetadata().setCurrentWorkspace(workspace.getRowKey().toString());
            userRepository.save(session.getModelSession(), currentUser);
        }

        LOGGER.info("Saving workspace: " + workspace.getRowKey() + "\ntitle: " + workspace.getMetadata().getTitle() + "\ndata: " + data);

        if (data != null) {
            workspace.getContent().setData(data);
        }


        workspaceRepository.save(session.getModelSession(), workspace);
        JSONObject resultJson = new JSONObject();
        resultJson.put("_rowKey", workspace.getRowKey().toString());
        resultJson.put("title", workspace.getMetadata().getTitle());

        respondWithJson(response, resultJson);
    }

    public Workspace handleNew(HttpServletRequest request) {
        User currentUser = getUser(request);
        WorkspaceRowKey workspaceRowKey = new WorkspaceRowKey(
                currentUser.getRowKey().toString(), String.valueOf(System.currentTimeMillis()));
        Workspace workspace = new Workspace(workspaceRowKey);
        String title = getOptionalParameter(request, "title");

        if (title != null) {
            workspace.getMetadata().setTitle(title);
        } else {
            workspace.getMetadata().setTitle(DEFAULT_WORKSPACE_TITLE + " - " + currentUser.getMetadata().getUserName());
        }

        return workspace;
    }
}
