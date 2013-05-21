package com.altamiracorp.web;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Router {
    public static enum Method { GET, POST, PUT, DELETE };
    private Map<Method, List<Route>> routes = new HashMap<Method, List<Route>>();

    public Router() {
        routes.put(Method.GET, new ArrayList<Route>());
        routes.put(Method.POST, new ArrayList<Route>());
        routes.put(Method.PUT, new ArrayList<Route>());
        routes.put(Method.DELETE, new ArrayList<Route>());
    }

    public void addRoute(Method method, String path, RequestHandler handler) {
        List<Route> methodRoutes = routes.get(method);
        Route route = new Route(path, handler);
        methodRoutes.add(route);
    }

    public void route(HttpServletRequest request, HttpServletResponse response) throws Exception {
        Method method = Method.valueOf(request.getMethod().toUpperCase());

        if (method == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        Route route = findRoute(method, request);

        if (route == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        RequestHandler handler = route.getHandler();
        handler.handle(request, response);
    }

    private Route findRoute(Method method, HttpServletRequest request) {
        List<Route> potentialRoutes = routes.get(method);
        for (Route route : potentialRoutes) {
            if (route.isMatch(request)) {
                return route;
            }
        }

        return null;
    }
}
