package com.altamiracorp.web;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class TestRequestHandler implements RequestHandler {
    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response) throws Exception {
        request.setAttribute("handled", "true");
    }
}
