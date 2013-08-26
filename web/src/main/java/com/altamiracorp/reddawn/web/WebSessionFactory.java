package com.altamiracorp.reddawn.web;

import javax.servlet.http.HttpServletRequest;

import com.altamiracorp.reddawn.RedDawnSession;

public class WebSessionFactory {

    public static RedDawnSession createRedDawnSession(HttpServletRequest request) {
        // TODO create a reddawn session based on user in request object.
        return RedDawnSession.create();
    }
}
