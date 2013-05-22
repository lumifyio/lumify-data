package com.altamiracorp.web;

import javax.servlet.http.HttpServletRequest;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Route {
    public static enum Method { GET, POST, PUT, DELETE };

    private Method method;
    private String path;
    private Handler[] handlers;

    private Pattern componentPattern = Pattern.compile("\\{([a-zA-Z]+)\\}");
    private String componentSplitRegex = "[/\\.]";
    private String[] routePathComponents;

    public Route(Method method, String path, Handler... handlers) {
        this.method = method;
        this.path = path;
        this.handlers = handlers;
        routePathComponents = path.split(componentSplitRegex);
    }

    public boolean isMatch(HttpServletRequest request) {
        Method requestMethod = Method.valueOf(request.getMethod().toUpperCase());
        if (!requestMethod.equals(method)) {
            return false;
        }

        String[] requestPathComponents = request.getPathInfo().split(componentSplitRegex);
        if (requestPathComponents.length != routePathComponents.length) {
            return false;
        }

        for (int i = 0; i < routePathComponents.length; i++) {
            String routeComponent = routePathComponents[i];
            String requestComponent = requestPathComponents[i];

            Matcher matcher = componentPattern.matcher(routeComponent);
            if (matcher.matches()) {
                request.setAttribute(matcher.group(1), requestComponent);
            } else if (!routeComponent.equals(requestComponent)) {
                return false;
            }
        }

        return true;
    }

    public Handler[] getHandlers() {
        return handlers;
    }

    public Method getMethod() {
        return method;
    }

    public String getPath() {
        return path;
    }
}
