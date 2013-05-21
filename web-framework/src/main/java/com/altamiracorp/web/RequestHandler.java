package com.altamiracorp.web;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public interface RequestHandler {
    public void handle(HttpServletRequest request, HttpServletResponse response) throws Exception;
}
