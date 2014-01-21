package com.altamiracorp.lumify.demoaccountweb.routes;

import com.altamiracorp.miniweb.HandlerChain;
import org.apache.commons.io.FileUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class CreateAccountForm extends BaseRequestHandler {

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, HandlerChain chain) throws Exception {
        String token = getOptionalParameter(request, "token");
        String errorMessage = getOptionalParameter(request, "err");

        if (token == null) {
            response.sendRedirect("index.html");
            return;
        }

        // TODO: get email based on token

        String path = request.getServletContext().getRealPath(request.getPathInfo());

        Map<String, String> replacementTokens = new HashMap();
        replacementTokens.put("email", "sample@example.com");
        replacementTokens.put("token", token);
        if (errorMessage != null) {
            replacementTokens.put("errorMessage", errorMessage);
        }

        String contents = replaceTokens(FileUtils.readFileToString(new File(path + ".html")), replacementTokens);
        response.setContentType("text/html");
        response.getOutputStream().write(contents.getBytes());
    }
}
