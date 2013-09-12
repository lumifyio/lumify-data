package com.altamiracorp.lumify.web;

import com.altamiracorp.lumify.model.user.User;
import com.altamiracorp.web.App;
import com.altamiracorp.web.AppAware;
import com.altamiracorp.web.Handler;
import com.altamiracorp.web.HandlerChain;
import com.altamiracorp.web.utils.UrlUtils;
import com.google.common.base.Preconditions;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Represents the base behavior that a {@link Handler} must support
 * and provides common methods for handler usage
 */
public abstract class BaseRequestHandler implements Handler, AppAware {
    protected WebApp app;

    @Override
    public void setApp(App application) {
        app = (WebApp) application;
    }


    @Override
    public abstract void handle(HttpServletRequest request, HttpServletResponse response, HandlerChain chain) throws Exception;

    /**
     * Attempts to extract the specified parameter from the provided request
     * @param request The request instance containing the parameter
     * @param parameterName The name of the parameter to extract
     * @return The value of the specified parameter
     * @throws RuntimeException Thrown if the required parameter was not in the request
     */
    protected String getRequiredParameter(final HttpServletRequest request, final String parameterName) {
        Preconditions.checkNotNull(request, "The provided request was invalid");

        return getParameter(request, parameterName, false);
    }

    protected long getOptionalParameterLong(final HttpServletRequest request, final String parameterName, long defaultValue) {
        String val = getOptionalParameter(request, parameterName);
        if(val == null) {
            return defaultValue;
        }
        return Long.parseLong(val);
    }

    protected double getOptionalParameterDouble(final HttpServletRequest request, final String parameterName, double defaultValue) {
        String val = getOptionalParameter(request, parameterName);
        if(val == null) {
            return defaultValue;
        }
        return Double.parseDouble(val);
    }

    /**
     * Attempts to extract the specified parameter from the provided request and convert it to a long value
     * @param request The request instance containing the parameter
     * @param parameterName The name of the parameter to extract
     * @return The long value of the specified parameter
     * @throws RuntimeException Thrown if the required parameter was not in the request
     */
    protected long getRequiredParameterAsLong(final HttpServletRequest request, final String parameterName) {
        return Long.parseLong(getRequiredParameter(request, parameterName));
    }


    /**
     * Attempts to extract the specified parameter from the provided request and convert it to a double value
     * @param request The request instance containing the parameter
     * @param parameterName The name of the parameter to extract
     * @return The double value of the specified parameter
     * @throws RuntimeException Thrown if the required parameter was not in the request
     */
    protected double getRequiredParameterAsDouble(final HttpServletRequest request, final String parameterName) {
        return Double.parseDouble(getRequiredParameter(request, parameterName));
    }


    /**
     * Attempts to extract the specified parameter from the provided request, if available
     * @param request The request instance containing the parameter
     * @param parameterName The name of the parameter to extract
     * @return The value of the specified parameter if found, null otherwise
     */
    protected String getOptionalParameter(final HttpServletRequest request, final String parameterName) {
        Preconditions.checkNotNull(request, "The provided request was invalid");

        return getParameter(request, parameterName, true);
    }


    private String getParameter(final HttpServletRequest request, final String parameterName, final boolean optional) {
        final String paramValue = request.getParameter(parameterName);

        if( paramValue == null ) {
            if( !optional ) {
                throw new RuntimeException(String.format("Parameter: '%s' is required in the request", parameterName));
            }

            return null;
        }

        return UrlUtils.urlDecode(paramValue);
    }

    protected String getAttributeString(final HttpServletRequest request, final String name) {
        return (String)request.getAttribute(name);
    }

    /**
     * Configures the content type for the provided response to contain {@link JSONObject} data
     * @param response The response instance to modify
     * @param jsonObject The JSON data to include in the response
     */
    protected void respondWithJson(final HttpServletResponse response, final JSONObject jsonObject) {
        configureResponse(ResponseTypes.JSON_OBJECT, response, jsonObject);
    }


    /**
     * Configures the content type for the provided response to contain {@link JSONArray} data
     * @param response The response instance to modify
     * @param jsonArray The JSON data to include in the response
     */
    protected void respondWithJson(final HttpServletResponse response, final JSONArray jsonArray) {
        configureResponse(ResponseTypes.JSON_ARRAY, response, jsonArray);
    }

    protected User getUser(HttpServletRequest request) {
        return AuthenticationHandler.getUser(request);
    }

    private void configureResponse(final ResponseTypes type, final HttpServletResponse response, final Object responseData) {
        Preconditions.checkNotNull(response, "The provided response was invalid");
        Preconditions.checkNotNull(responseData, "The provided data was invalid");

        final Responder responder = new Responder(response);

        try {
            switch(type) {
                case JSON_OBJECT:
                    responder.respondWith((JSONObject)responseData);
                    break;
                case JSON_ARRAY:
                    responder.respondWith((JSONArray)responseData);
                    break;
                default:
                    throw new RuntimeException("Unsupported response type encountered");
            }
        } catch (IOException e) {
            throw new RuntimeException("Error occurred while writing response", e);
        }
    }
}
