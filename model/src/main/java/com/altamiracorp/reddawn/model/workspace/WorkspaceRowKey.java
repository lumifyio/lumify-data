package com.altamiracorp.reddawn.model.workspace;

import com.altamiracorp.reddawn.model.RowKeyHelper;
import com.altamiracorp.reddawn.model.RowKey;

public class WorkspaceRowKey extends RowKey {
    public WorkspaceRowKey(String rowKey) {
        super(rowKey);
    }

    public WorkspaceRowKey(String userId, String workspaceId) {
        super(RowKeyHelper.build(userId, workspaceId));
    }
}
