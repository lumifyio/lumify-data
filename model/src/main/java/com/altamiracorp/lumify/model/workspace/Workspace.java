package com.altamiracorp.lumify.model.workspace;

import com.altamiracorp.lumify.core.user.User;
import com.altamiracorp.lumify.model.Column;
import com.altamiracorp.lumify.model.Row;
import com.altamiracorp.lumify.model.RowKey;
import com.altamiracorp.lumify.model.Value;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Workspace extends Row<WorkspaceRowKey> {
    public static final String TABLE_NAME = "atc_Workspace";

    public Workspace(WorkspaceRowKey rowKey) {
        super(TABLE_NAME, rowKey);
    }

    public Workspace(String userId, String workspaceId) {
        super(TABLE_NAME, new WorkspaceRowKey(userId, workspaceId));
    }

    public Workspace(RowKey rowKey) {
        super(TABLE_NAME, new WorkspaceRowKey(rowKey.toString()));
    }

    public WorkspaceContent getContent() {
        WorkspaceContent artifactContent = get(WorkspaceContent.NAME);
        if (artifactContent == null) {
            addColumnFamily(new WorkspaceContent());
        }
        return get(WorkspaceContent.NAME);
    }

    public WorkspaceMetadata getMetadata() {
        WorkspaceMetadata workspaceMetadata = get(WorkspaceMetadata.NAME);
        if (workspaceMetadata == null) {
            addColumnFamily(new WorkspaceMetadata());
        }
        return get(WorkspaceMetadata.NAME);
    }

    public WorkspacePermissions getPermissions() {
        WorkspacePermissions workspaceUsers = get(WorkspacePermissions.NAME);
        if (workspaceUsers == null) {
            addColumnFamily(new WorkspacePermissions());
        }
        return get(WorkspacePermissions.NAME);
    }


    public JSONObject toJson(User user) {
        try {
            JSONObject workspaceJson = new JSONObject();
            workspaceJson.put("_rowKey", getRowKey());
            workspaceJson.put("title", getMetadata().getTitle());
            workspaceJson.put("createdBy", getMetadata().getCreator());
            workspaceJson.put("isSharedToUser", !getMetadata().getCreator().equals(user.getRowKey()));

            Boolean hasAccess = false;
            Boolean hasEdit = false;

            if (getMetadata().getCreator().equals(user.getRowKey())) {
                hasAccess = true;
                hasEdit = true;
            }

            JSONArray permissions = new JSONArray();
            if (get(WorkspacePermissions.NAME) != null) {
                for (Column col : get(WorkspacePermissions.NAME).getColumns()) {
                    String rowKey = col.getName();
                    JSONObject users = new JSONObject();
                    JSONObject userPermissions = Value.toJson(col.getValue());
                    users.put("user", rowKey);
                    users.put("userPermissions", userPermissions);
                    permissions.put(users);
                    if (rowKey.equals(user.getRowKey())) {
                        if (userPermissions.getBoolean("edit")) {
                            hasEdit = true;
                        }
                        hasAccess = true;
                    }
                }

                workspaceJson.put("permissions", permissions);
            }

            if (hasAccess) {
                workspaceJson.put("isEditable", hasEdit);
                return workspaceJson;
            }

            return null;
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }
}
