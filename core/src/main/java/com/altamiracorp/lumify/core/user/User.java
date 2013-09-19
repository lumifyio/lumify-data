package com.altamiracorp.lumify.core.user;

public class User {
    private String username;
    private String rowKey;
    private String currentWorkspace;

    public ModelAuthorizations getAuthorizations() {
        return new ModelAuthorizations(); // TODO: change to AccumuloAuthorizations
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
