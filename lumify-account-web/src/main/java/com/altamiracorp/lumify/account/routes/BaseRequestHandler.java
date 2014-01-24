package com.altamiracorp.lumify.account.routes;

import com.altamiracorp.miniweb.Handler;
import com.altamiracorp.miniweb.utils.UrlUtils;
import com.google.common.base.Preconditions;
import org.json.JSONObject;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

public abstract class BaseRequestHandler implements Handler {
    protected void respondWithJson(final HttpServletResponse response, final JSONObject json) throws IOException {
        response.setContentType("application/json");
        response.getWriter().write(json.toString());
    }
    protected String getBaseUrl(final HttpServletRequest request) {
        String path = request.getServletContext().getRealPath(request.getPathInfo());
        String url = request.getRequestURL().toString();
        int slashIndex = url.indexOf('/', "https://".length() + 1);
        return url.substring(0, slashIndex) + request.getServletContext().getContextPath();
    }

    protected boolean getParameterBoolean(final HttpServletRequest request, final String parameterName) {
        String parameter = getParameter(request, parameterName, true);
        return parameter != null && (parameter.equals("on") || parameter.equals("1") || parameter.equals("true"));
    }

    protected String getRequiredParameter(final HttpServletRequest request, final String parameterName) {
        Preconditions.checkNotNull(request, "The provided request was invalid");
        return getParameter(request, parameterName, false);
    }

    protected String getOptionalParameter(final HttpServletRequest request, final String parameterName) {
        return getParameter(request, parameterName, true);
    }

    protected String replaceTokens(String content, Map<String, String> tokens) {
        for (Map.Entry<String, String> entry : tokens.entrySet()) {
            content = content.replaceAll("\\{\\{\\s*" + entry.getKey() + "\\s*\\}\\}", entry.getValue());
        }

        // Empty any not matched
        content = content.replaceAll("\\{\\{\\s*.*?\\s*\\}\\}", "");

        return content;
    }

    private String getParameter(final HttpServletRequest request, final String parameterName, final boolean optional) {
        final String paramValue = request.getParameter(parameterName);

        if (paramValue == null) {
            if (!optional) {
                throw new RuntimeException(String.format("Parameter: '%s' is required in the request", parameterName));
            }

            return null;
        }

        return UrlUtils.urlDecode(paramValue);
    }
}
