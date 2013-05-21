package com.altamiracorp.web;

import javax.servlet.http.HttpServletRequest;

public class Route {
    private RequestHandler handler;
    private String[] routePathComponents;

    public Route(String path, RequestHandler handler) {
        this.handler = handler;
        routePathComponents = path.split("/");
    }

    public boolean isMatch(HttpServletRequest request) {
        String[] requestPathComponents = request.getPathInfo().split("/");

        if (requestPathComponents.length != routePathComponents.length) {
            return false;
        }

        for (int i = 0; i < routePathComponents.length; i++) {
            String routeComponent = routePathComponents[i];
            String requestComponent = requestPathComponents[i];

            if (routeComponent.startsWith(":")) {
                request.setAttribute(routeComponent.substring(1), requestComponent);
            } else if (!routeComponent.equals(requestComponent)) {
                return false;
            }
        }

        return true;
    }

    public RequestHandler getHandler() {
        return handler;
    }
}
