package com.altamiracorp.reddawn.web;

import com.altamiracorp.reddawn.RedDawnSession;

import javax.servlet.http.HttpServletRequest;

public class WebSessionFactory {
    private static Server server;

    public static void setServer(Server server) {
        WebSessionFactory.server = server;
    }

    public static RedDawnSession createRedDawnSession(HttpServletRequest request) {
        return server.createRedDawnSession(request);
    }
}
