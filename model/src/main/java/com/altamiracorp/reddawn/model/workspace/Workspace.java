package com.altamiracorp.reddawn.model.workspace;

import com.altamiracorp.reddawn.model.Row;
import com.altamiracorp.reddawn.model.RowKey;

public class Workspace extends Row<WorkspaceRowKey> {
    public static final String TABLE_NAME = "Workspace";
    private static final String DATA = "data";

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
}
