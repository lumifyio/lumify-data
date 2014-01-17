package com.altamiracorp.lumify.demoaccountweb.routes;

import com.altamiracorp.lumify.demoaccountweb.security.AuthenticationProvider;
import com.altamiracorp.miniweb.HandlerChain;
import com.google.inject.Inject;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class CreateToken extends BaseRequestHandler {
    private final AuthenticationProvider authenticationProvider;

    @Inject
    public CreateToken(AuthenticationProvider authenticationProvider) {
        this.authenticationProvider = authenticationProvider;
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, HandlerChain chain) throws Exception {
        // FIXME
    }
}
