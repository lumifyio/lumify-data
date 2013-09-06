package com.altamiracorp.reddawn.web.routes.user;

import com.altamiracorp.reddawn.model.user.User;
import com.altamiracorp.reddawn.web.DevBasicAuthenticator;
import com.altamiracorp.reddawn.web.Responder;
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
