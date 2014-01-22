package com.altamiracorp.lumify.demoaccountweb.routes;

import com.altamiracorp.lumify.demoaccountweb.DemoAccountUserRepository;
import com.altamiracorp.lumify.demoaccountweb.model.DemoAccountUser;
import com.altamiracorp.miniweb.HandlerChain;
import com.google.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class CreateToken extends BaseRequestHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(CreateToken.class.getName());
    private DemoAccountUserRepository demoAccountUserRepository;

    @Inject
    public void setDemoAccountUserRepository(DemoAccountUserRepository demoAccountUserRepository) {
        this.demoAccountUserRepository = demoAccountUserRepository;
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, HandlerChain chain) throws Exception {
        String email = getRequiredParameter(request, "email");
        boolean shouldRegister = getRequiredParameterBoolean(request, "register");

        DemoAccountUser user = demoAccountUserRepository.getOrCreateUser(email, shouldRegister);
        demoAccountUserRepository.generateToken(user);

        LOGGER.debug("Generated token: " + user.getMetadata().getToken());
        LOGGER.debug("Token Expiration: " + user.getMetadata().getTokenExpiration());

        demoAccountUserRepository.save(user);

        // TODO: Generate email with token

        response.sendRedirect("token-created.html");
    }
}
