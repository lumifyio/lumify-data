package com.altamiracorp.lumify.core.user;

public class User {
    private String username;

    public ModelAuthorizations getAuthorizations() {
        return new ModelAuthorizations(); // TODO: change to AccumuloAuthorizations
    }

    public String getUsername() {
        return username;
    }
}
