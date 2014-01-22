package com.altamiracorp.lumify.demoaccountweb.routes;

import com.altamiracorp.lumify.demoaccountweb.DemoAccountUserRepository;
import com.altamiracorp.lumify.demoaccountweb.model.DemoAccountUser;
import com.altamiracorp.miniweb.HandlerChain;
import com.google.inject.Inject;
import org.apache.commons.io.FileUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class CreateAccountForm extends BaseRequestHandler {
    private DemoAccountUserRepository demoAccountUserRepository;

    @Inject
    public void setDemoAccountUserRepository(DemoAccountUserRepository demoAccountUserRepository) {
        this.demoAccountUserRepository = demoAccountUserRepository;
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, HandlerChain chain) throws Exception {
        String token = getOptionalParameter(request, "token");
        String errorMessage = getOptionalParameter(request, "err");

        if (token == null) {
            response.sendRedirect("index.html");
            return;
        }

        DemoAccountUser user =  demoAccountUserRepository.getUserFromToken(token);
        if (user == null) {
            response.sendRedirect("index.html");
            return;
        }

        String path = request.getServletContext().getRealPath(request.getPathInfo());

        Map<String, String> replacementTokens = new HashMap();
        replacementTokens.put("email", user.getMetadata().getEmail());
        replacementTokens.put("token", token);
        if (errorMessage != null) {
            replacementTokens.put("errorMessage", errorMessage);
        }

        String contents = replaceTokens(FileUtils.readFileToString(new File(path + ".html")), replacementTokens);
        response.setContentType("text/html");
        response.getOutputStream().write(contents.getBytes());
    }
}
