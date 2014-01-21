package com.altamiracorp.lumify.demoaccountweb.routes;

import com.altamiracorp.miniweb.HandlerChain;
import com.altamiracorp.miniweb.utils.UrlUtils;
import com.google.inject.Inject;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class CreateAccount extends BaseRequestHandler {

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, HandlerChain chain) throws Exception {
        String token = getRequiredParameter(request, "token");
        String email = "sample@example.com"; // Get email from token, not from request
        String password = getRequiredParameter(request, "password");
        String confirmPassword = getRequiredParameter(request, "confirm-password");


        if (!password.equals(confirmPassword)) {
            String path = "create-account?err=" + UrlUtils.urlEncode("Passwords do not match") + "&token=" + token;
            response.sendRedirect(path);
            return;
        }

        // TODO update user account

        response.sendRedirect("account-created.html");
    }
}
