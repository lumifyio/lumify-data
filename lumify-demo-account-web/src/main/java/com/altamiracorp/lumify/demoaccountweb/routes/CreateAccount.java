package com.altamiracorp.lumify.demoaccountweb.routes;

import com.altamiracorp.lumify.core.model.user.UserRepository;
import com.altamiracorp.lumify.core.user.SystemUser;
import com.altamiracorp.lumify.demoaccountweb.DemoAccountUserRepository;
import com.altamiracorp.lumify.demoaccountweb.model.DemoAccountUser;
import com.altamiracorp.miniweb.HandlerChain;
import com.altamiracorp.miniweb.utils.UrlUtils;
import com.google.inject.Inject;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;

public class CreateAccount extends BaseRequestHandler {
    private UserRepository userRepository;
    private DemoAccountUserRepository demoAccountUserRepository;

    @Inject
    public void setDemoAccountUserRepository(DemoAccountUserRepository demoAccountUserRepository) {
        this.demoAccountUserRepository = demoAccountUserRepository;
    }

    @Inject
    public void setUserRepository(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, HandlerChain chain) throws Exception {
        String token = getRequiredParameter(request, "token");
        String password = getRequiredParameter(request, "password");
        String confirmPassword = getRequiredParameter(request, "confirm-password");


        if (!password.equals(confirmPassword)) {
            String path = "create-account?err=" + UrlUtils.urlEncode("Passwords do not match") + "&token=" + token;
            response.sendRedirect(path);
            return;
        }

        DemoAccountUser user = demoAccountUserRepository.getUserFromToken(token);
        if (user == null) {
            response.sendRedirect("index.html");
            return;
        }

        userRepository.addUser(user.getMetadata().getEmail(), password, new SystemUser());

        // expire the token
        user.getMetadata().setTokenExpiration(new Date());
        demoAccountUserRepository.save(user);

        response.sendRedirect("account-created.html");
    }
}
