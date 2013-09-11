package com.altamiracorp.lumify.web;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.codec.binary.Base64;

import com.altamiracorp.lumify.AppSession;
import com.altamiracorp.lumify.model.user.User;
import com.altamiracorp.lumify.model.user.UserRepository;
import com.altamiracorp.web.HandlerChain;

public class DevBasicAuthenticator extends BaseRequestHandler {
    private static final String HTTP_BASIC_REALM = "lumify";
    private static final String HTTP_AUTHORIZATION_HEADER = "Authorization";
    private static final String HTTP_AUTHENTICATE_HEADER = "WWW-Authenticate";
    private static final int HTTP_NOT_AUTHORIZED_ERROR_CODE = 401;
    private UserRepository userRepository = new UserRepository();

    public static User getUser(HttpServletRequest request) {
        return getUser(request.getSession());
    }

    public static User getUser(HttpSession session) {
        return (User) session.getAttribute("user.current");
    }

    public static void setUser(HttpServletRequest request, User user) {
        request.getSession().setAttribute("user.current", user);
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, HandlerChain chain) throws Exception {
        if (isAuthorizationPresent(request)) {
            String username = parseUsernameFromAuthorization(request);
            if (username == null) {
                requestAuthorization(response);
            } else {
                AppSession session = app.getAppSession(request);

                User user = userRepository.findOrAddUser(session.getModelSession(), username);
                setUser(request, user);
                chain.next(request, response);
            }
        } else {
            requestAuthorization(response);
        }
    }

    private void requestAuthorization(HttpServletResponse response) throws IOException {
        response.setHeader(HTTP_AUTHENTICATE_HEADER, "Basic realm=\"" + HTTP_BASIC_REALM + "\"");
        response.sendError(HTTP_NOT_AUTHORIZED_ERROR_CODE);
    }

    private String parseUsernameFromAuthorization(HttpServletRequest request) {
        String base64Auth = request.getHeader(HTTP_AUTHORIZATION_HEADER);
        String[] authComponents = base64Auth.split(" ");

        if (authComponents.length == 2 && authComponents[0].equals("Basic")) {
            String usernamePasswordCombo = new String(Base64.decodeBase64(authComponents[1]));
            String[] usernamePassword = usernamePasswordCombo.split(":");
            if (usernamePassword.length == 2) {
                return usernamePassword[0];
            }
        }

        return null;
    }

    private boolean isAuthorizationPresent(HttpServletRequest request) {
        return request.getHeader(HTTP_AUTHORIZATION_HEADER) != null;
    }
}
