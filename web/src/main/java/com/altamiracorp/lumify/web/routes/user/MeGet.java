package com.altamiracorp.lumify.web.routes.user;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.altamiracorp.lumify.model.user.User;
import com.altamiracorp.lumify.web.BaseRequestHandler;
import com.altamiracorp.lumify.web.DevBasicAuthenticator;
import com.altamiracorp.web.HandlerChain;

public class MeGet extends BaseRequestHandler {

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, HandlerChain chain) throws Exception {
        User user = DevBasicAuthenticator.getUser(request);

        respondWithJson(response, user.toJson());
    }
}
