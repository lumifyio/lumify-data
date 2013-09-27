package com.altamiracorp.lumify.model.workspace;

import com.altamiracorp.lumify.model.Row;
import com.altamiracorp.lumify.model.RowKey;

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

    public WorkspacePermissions getUsers () {
        WorkspacePermissions workspaceUsers = get(WorkspacePermissions.NAME);
        if (workspaceUsers == null) {
            addColumnFamily(new WorkspacePermissions());
        }
        return get(WorkspacePermissions.NAME);
    }
}
