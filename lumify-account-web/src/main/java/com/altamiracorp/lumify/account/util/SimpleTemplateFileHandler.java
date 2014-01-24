package com.altamiracorp.lumify.account.util;

import com.altamiracorp.lumify.account.routes.BaseRequestHandler;
import com.altamiracorp.miniweb.Handler;
import com.altamiracorp.miniweb.HandlerChain;
import org.apache.commons.io.FileUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;

public class SimpleTemplateFileHandler extends BaseRequestHandler {
    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, HandlerChain chain) throws Exception {
        String path = request.getServletContext().getRealPath(request.getPathInfo());
        String contents = FileUtils.readFileToString(new File(path));
        contents = contents.replaceAll("\\$\\{context.url}", getBaseUrl(request));

        response.setContentType("text/html");
        response.getOutputStream().write(contents.getBytes());
    }
}
