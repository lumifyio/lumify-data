package com.altamiracorp.web;

import javax.servlet.http.HttpServletRequest;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Route {
    private Pattern componentPattern = Pattern.compile("\\{([a-zA-Z]+)\\}");
    private String componentSplitRegex = "[/\\.]";
    private RequestHandler handler;
    private String[] routePathComponents;

    public Route(String path, RequestHandler handler) {
        this.handler = handler;
        routePathComponents = path.split(componentSplitRegex);
    }

    public boolean isMatch(HttpServletRequest request) {
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

    public RequestHandler getHandler() {
        return handler;
    }
}
