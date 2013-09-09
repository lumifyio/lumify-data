package com.altamiracorp.lumify.web;

import com.altamiracorp.lumify.AppSession;

import javax.servlet.http.HttpServletRequest;

public class WebSessionFactory {

    private static AppSession session;

    public static AppSession createAppSession(HttpServletRequest request) {
        // TODO create a lumify session based on user in request object.
        if (session != null) {
            return session;
        }
        session = AppSession.create();
        return session;
    }
}
