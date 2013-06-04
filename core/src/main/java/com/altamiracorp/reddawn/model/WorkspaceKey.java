package com.altamiracorp.reddawn.model;

import com.altamiracorp.reddawn.ucd.model.KeyHelpers;

public class WorkspaceKey {
    private String userId;
    private String workspaceId;

    public WorkspaceKey(String rowKey) {
        String[] parts = KeyHelpers.splitOnSeperator(rowKey);
        if (parts.length != 2) {
            throw new RuntimeException("Invalid workspace key \"" + rowKey + "\"");
        }
        this.userId = parts[0];
        this.workspaceId = parts[1];
    }

    public WorkspaceKey(String userId, String workspaceId) {
        this.userId = userId;
        this.workspaceId = workspaceId;
    }

    @Override
    public String toString() {
        return KeyHelpers.createCompositeKey(getUserId(), getWorkspaceId());
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getWorkspaceId() {
        return workspaceId;
    }

    public void setWorkspaceId(String workspaceId) {
        this.workspaceId = workspaceId;
    }
}
