package com.altamiracorp.lumify.core.user;

public class User {
    private String username;
    private String rowKey;
    private String currentWorkspace;
    private ModelAuthorizations modelAuthorizations;

    public User(String rowKey, String username, String currentWorkspace, ModelAuthorizations modelAuthorizations) {
        this.rowKey = rowKey;
        this.username = username;
        this.currentWorkspace = currentWorkspace;
        this.modelAuthorizations = modelAuthorizations;
    }

    public ModelAuthorizations getModelAuthorizations() {
        return modelAuthorizations;
    }

    public String getUsername() {
        return username;
    }

    public String getRowKey() {
        return rowKey;
    }

    public String getCurrentWorkspace() {
        return currentWorkspace;
    }

    public void setCurrentWorkspace(String currentWorkspace) {
        this.currentWorkspace = currentWorkspace;
    }
}
