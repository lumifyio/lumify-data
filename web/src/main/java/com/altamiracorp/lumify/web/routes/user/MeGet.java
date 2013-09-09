package com.altamiracorp.lumify.web.routes.user;

import com.altamiracorp.lumify.model.user.User;
import com.altamiracorp.lumify.web.DevBasicAuthenticator;
import com.altamiracorp.lumify.web.Responder;
import com.altamiracorp.web.Handler;
import com.altamiracorp.web.HandlerChain;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class MeGet implements Handler {

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, HandlerChain chain) throws Exception {
        User user = DevBasicAuthenticator.getUser(request);
        new Responder(response).respondWith(user.toJson());
    }

}
