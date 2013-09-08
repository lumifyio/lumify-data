package com.altamiracorp.lumify.model.workspace;

import com.altamiracorp.lumify.model.RowKey;
import com.altamiracorp.lumify.model.RowKeyHelper;

public class WorkspaceRowKey extends RowKey {
    public WorkspaceRowKey(String rowKey) {
        super(rowKey);
    }

    public WorkspaceRowKey(String userId, String workspaceId) {
        super(RowKeyHelper.buildMinor(userId, workspaceId));
    }
}
