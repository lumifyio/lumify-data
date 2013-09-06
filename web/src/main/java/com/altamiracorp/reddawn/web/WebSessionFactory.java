package com.altamiracorp.reddawn.web;

import com.altamiracorp.reddawn.RedDawnSession;

import javax.servlet.http.HttpServletRequest;

public class WebSessionFactory {

    private static RedDawnSession session;

    public static RedDawnSession createRedDawnSession(HttpServletRequest request) {
        // TODO create a reddawn session based on user in request object.
        if (session != null) {
            return session;
        }
        session = RedDawnSession.create();
        return session;
    }
}
