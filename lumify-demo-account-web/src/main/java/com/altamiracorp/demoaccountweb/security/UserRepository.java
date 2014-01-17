package com.altamiracorp.demoaccountweb.security;

public abstract class UserRepository {
    public abstract User validateUser(String username, String password);
}
