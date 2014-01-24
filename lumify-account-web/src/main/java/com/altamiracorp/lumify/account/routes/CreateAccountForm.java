package com.altamiracorp.lumify.account.routes;

import com.altamiracorp.lumify.account.AccountUserRepository;
import com.altamiracorp.lumify.account.model.AccountUser;
import com.altamiracorp.miniweb.HandlerChain;
import com.google.inject.Inject;
import org.apache.commons.io.FileUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class CreateAccountForm extends BaseRequestHandler {
    private AccountUserRepository accountUserRepository;

    @Inject
    public void setAccountUserRepository(AccountUserRepository accountUserRepository) {
        this.accountUserRepository = accountUserRepository;
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, HandlerChain chain) throws Exception {
        String token = getOptionalParameter(request, "token");
        String errorMessage = getOptionalParameter(request, "err");
        boolean reset = getParameterBoolean(request, "reset");

        if (token == null) {
            response.sendRedirect("index.html");
            return;
        }

        AccountUser user =  accountUserRepository.getUserFromToken(token);
        if (user == null || user.getData().getTokenExpiration().before(new Date())) {
            response.sendRedirect("index.html");
            return;
        }

        String path = request.getServletContext().getRealPath(request.getPathInfo());

        Map<String, String> replacementTokens = new HashMap();
        replacementTokens.put("email", user.getData().getEmail());
        replacementTokens.put("token", token);
        replacementTokens.put("baseUrl", getBaseUrl(request));
        replacementTokens.put("buttonText", reset ? "Reset Password" : "Create Account");
        replacementTokens.put("action", reset ? "create-account?reset=1" : "create-account");
        if (errorMessage != null) {
            replacementTokens.put("errorMessage", errorMessage);
        }

        String contents = replaceTokens(FileUtils.readFileToString(new File(path + ".html")), replacementTokens);
        response.setContentType("text/html");
        response.getOutputStream().write(contents.getBytes());
    }
}
