package com.altamiracorp.web;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public abstract class WebApp extends HttpServlet {
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
        } catch (Throwable e) {
            throw new ServletException(e);
        }
    }

    public void get(String path, RequestHandler handler) {
        router.addRoute(Router.Method.GET, path, handler);
    }

    public void get(String path, Class<? extends RequestHandler> clazz) {
        try {
            RequestHandler handler = clazz.newInstance();
            get(path, handler);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void post(String path, RequestHandler handler) {
        router.addRoute(Router.Method.POST, path, handler);
    }

    public void post(String path, Class<? extends RequestHandler> clazz) {
        try {
            RequestHandler handler = clazz.newInstance();
            post(path, handler);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void put(String path, RequestHandler handler) {
        router.addRoute(Router.Method.PUT, path, handler);
    }

    public void put(String path, Class<? extends RequestHandler> clazz) {
        try {
            RequestHandler handler = clazz.newInstance();
            put(path, handler);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void delete(String path, RequestHandler handler) {
        router.addRoute(Router.Method.DELETE, path, handler);
    }

    public void delete(String path, Class<? extends RequestHandler> clazz) {
        try {
            RequestHandler handler = clazz.newInstance();
            delete(path, handler);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public abstract void setup(ServletConfig config);
}
