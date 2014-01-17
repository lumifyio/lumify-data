package com.altamiracorp.demoaccountweb.routes;

import com.altamiracorp.demoaccountweb.security.AuthenticationProvider;
import com.altamiracorp.demoaccountweb.security.UserRepository;
import com.altamiracorp.miniweb.HandlerChain;
import com.google.inject.Inject;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class CreateToken extends BaseRequestHandler {
    private final UserRepository userRepository;
    private final AuthenticationProvider authenticationProvider;

    @Inject
    public CreateToken(AuthenticationProvider authenticationProvider, UserRepository userRepository) {
        this.authenticationProvider = authenticationProvider;
        this.userRepository = userRepository;
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, HandlerChain chain) throws Exception {
        // FIXME
    }
}
