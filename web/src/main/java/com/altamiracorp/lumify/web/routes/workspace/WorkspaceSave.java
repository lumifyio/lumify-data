package com.altamiracorp.lumify.web.routes.workspace;

import com.altamiracorp.lumify.core.user.User;
import com.altamiracorp.lumify.model.Column;
import com.altamiracorp.lumify.model.ModelSession;
import com.altamiracorp.lumify.model.Row;
import com.altamiracorp.lumify.model.user.UserRepository;
import com.altamiracorp.lumify.model.workspace.Workspace;
import com.altamiracorp.lumify.model.workspace.WorkspacePermissions;
import com.altamiracorp.lumify.model.workspace.WorkspaceRepository;
import com.altamiracorp.lumify.model.workspace.WorkspaceRowKey;
import com.altamiracorp.lumify.web.BaseRequestHandler;
import com.altamiracorp.web.HandlerChain;
import com.google.inject.Inject;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;

public class WorkspaceSave extends BaseRequestHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(WorkspaceSave.class);
    private static final String DEFAULT_WORKSPACE_TITLE = "Default";

    private final WorkspaceRepository workspaceRepository;
    private final UserRepository userRepository;
    private final ModelSession modelSession;

    @Inject
    public WorkspaceSave(final WorkspaceRepository workspaceRepository, final UserRepository userRepository, final ModelSession modelSession) {
        this.workspaceRepository = workspaceRepository;
        this.userRepository = userRepository;
        this.modelSession = modelSession;
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
            authUser.setCurrentWorkspace(workspace.getRowKey().toString());
            userRepository.save(user, authUser);
        }

        LOGGER.info("Saving workspace: " + workspace.getRowKey() + "\ntitle: " + workspace.getMetadata().getTitle() + "\ndata: " + data);

        if (data != null) {
            workspace.getContent().setData(data);
            if (new JSONObject(data).keySet().contains("users")) {
                workspace.getUsers();

                // Getting user permissions
                JSONArray userList = new JSONObject(data).getJSONArray("users");
                String userRowkey = user.getRowKey().toString();

                if (workspace.getMetadata().getCreator().equals(userRowkey) ||
                        hasWritePermissions(userRowkey, workspace, userList)) {
                    updateUserList(workspace, userList, authUser);
                }
            }
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

        workspace.getMetadata().setCreator(user.getRowKey().toString());

        return workspace;
    }

    private void updateUserList(Workspace workspace, JSONArray userList, com.altamiracorp.lumify.core.user.User user) {
        List<Column> deleteList = new ArrayList<Column>();
        List<Column> addList = new ArrayList<Column>();

        Row<WorkspaceRowKey> rowKey = workspaceRepository.toRow(workspace);
        for (Column column : workspace.getUsers().getColumns()) {
            boolean added = false;
            for (int i = 0; i < userList.length(); i++) {
                JSONObject obj = userList.getJSONObject(i);
                Column col = new Column(obj.getString("user"), obj.getJSONObject("permissions"));
                if (!addList.contains(col)) {
                    addList.add(col);
                }

                if (column.getName().equals(obj.getString("user"))) {
                    added = true;
                }
            }
            if (!added) {
                column.setDirty(true);
                deleteList.add(column);
            }
        }
        modelSession.deleteColumnsList(rowKey, Workspace.TABLE_NAME, WorkspacePermissions.NAME, deleteList, user);

        modelSession.addManyCols(rowKey, Workspace.TABLE_NAME, WorkspacePermissions.NAME, addList, user);
    }

    private boolean hasWritePermissions(String user, Workspace workspace, JSONArray userList) {
        if (workspace.getUsers().get(user) != null) {
            JSONObject permissions = new JSONObject(workspace.getUsers().get(user));
            if (permissions.getBoolean("edit")) {
                return true;
            }
        } else {
            for (int i = 0; i < userList.length(); i++) {
                if (userList.getJSONObject(i).getString("user").equals(user) &&
                        userList.getJSONObject(i).getBoolean("edit")) {
                    return true;
                }
            }
        }
        return false;
    }
}
