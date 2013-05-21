package com.altamiracorp.web;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public abstract class WebApp implements Servlet {
    private Router router;

    @Override
    public void init(ServletConfig servletConfig) throws ServletException {
        this.router = new Router();
        setup(servletConfig);
    }

    @Override
    public void service(ServletRequest servletRequest, ServletResponse servletResponse) throws ServletException, IOException {
        try {
            router.route((HttpServletRequest)servletRequest, (HttpServletResponse)servletResponse);
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }

    public void get(String path, RequestHandler handler) {
        router.addRoute(Router.Method.GET, path, handler);
    }

    public void post(String path, RequestHandler handler) {
        router.addRoute(Router.Method.POST, path, handler);
    }

    public void put(String path, RequestHandler handler) {
        router.addRoute(Router.Method.PUT, path, handler);
    }

    public void delete(String path, RequestHandler handler) {
        router.addRoute(Router.Method.DELETE, path, handler);
    }

    public abstract void setup(ServletConfig config);
}
