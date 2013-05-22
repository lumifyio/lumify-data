package com.altamiracorp.web;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import com.altamiracorp.web.Route.Method;

public abstract class App extends HttpServlet {
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

    public void get(String path, Handler... handlers) {
        router.addRoute(Method.GET, path, handlers);
    }

    public void get(String path, Class<? extends Handler>... classes) {
        try {
            Handler[] handlers = instantiateHandlers(classes);
            get(path, handlers);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void post(String path, Handler... handlers) {
        router.addRoute(Method.POST, path, handlers);
    }

    public void post(String path, Class<? extends Handler>... classes) {
        try {
            Handler[] handlers = instantiateHandlers(classes);
            post(path, handlers);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void put(String path, Handler... handlers) {
        router.addRoute(Method.PUT, path, handlers);
    }

    public void put(String path, Class<? extends Handler>... classes) {
        try {
            Handler[] handlers = instantiateHandlers(classes);
            put(path, handlers);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void delete(String path, Handler... handlers) {
        router.addRoute(Method.DELETE, path, handlers);
    }

    public void delete(String path, Class<? extends Handler>... classes) {
        try {
            Handler[] handlers = instantiateHandlers(classes);
            delete(path, handlers);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private Handler[] instantiateHandlers(Class<? extends Handler>[] handlerClasses) throws Exception {
        Handler[] handlers = new Handler[handlerClasses.length];
        for (int i = 0; i < handlerClasses.length; i++) {
            handlers[i] = handlerClasses[i].newInstance();
        }
        return handlers;
    }

    public abstract void setup(ServletConfig config);
}
