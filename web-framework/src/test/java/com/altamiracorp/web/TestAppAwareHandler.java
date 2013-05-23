package com.altamiracorp.web;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class TestAppAwareHandler implements Handler, AppAware {
    private App app;

    @Override
    public void setApp(App app) {
        this.app = app;
    }

    public App getApp() {
        return app;
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, HandlerChain chain) throws Exception {
        request.setAttribute("handled", "true");
        chain.next(request, response);
    }
}
