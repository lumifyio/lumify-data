package com.altamiracorp.reddawn.web;

import javax.servlet.http.HttpServletRequest;

public class User {
    private final String username;

    public User(String username) {
        this.username = username;
    }

    public static User getUser(HttpServletRequest request) {
        return (User) request.getSession().getAttribute("user.current");
    }

    public static void setUser(HttpServletRequest request, User user) {
        request.getSession().setAttribute("user.current", user);
    }

    public String getUsername() {
        return username;
    }

    @Override
    public int hashCode() {
        return this.username.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof User) {
            final User other = (User) obj;
            return username.equals(other.username);
        } else {
            return false;
        }
    }

    public String getId() {
        String id = this.username.toLowerCase().replace(' ', '_');
        return id; // TODO change out to a better id
    }
}
