package com.altamiracorp.lumify.demoaccountweb.security;

public abstract class UserRepository {
    public abstract User validateUser(String username, String password);
}
