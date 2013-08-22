package com.altamiracorp.reddawn.web.routes.vertex;

import com.altamiracorp.reddawn.RedDawnSession;
import com.altamiracorp.reddawn.web.Responder;
import com.altamiracorp.reddawn.web.User;
import com.altamiracorp.reddawn.web.WebApp;
import com.altamiracorp.web.App;
import com.altamiracorp.web.AppAware;
import com.altamiracorp.web.Handler;
import com.altamiracorp.web.HandlerChain;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class VertexAddProperty implements Handler, AppAware {
    private static final Logger LOGGER = LoggerFactory.getLogger(VertexAddProperty.class.getName());
    private WebApp app;

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, HandlerChain chain) throws Exception {
        User currentUser = User.getUser(request);
        RedDawnSession session = app.getRedDawnSession(request);
        String vertexId = request.getParameter("vertexId");
        String propertyName = request.getParameter("propertyName");
        String value = request.getParameter("value");

        System.out.println("vertexId: " + vertexId);
        System.out.println("propertyName: " + propertyName);
        System.out.println("value: " + value);

        JSONObject json = new JSONObject();
        new Responder(response).respondWith(json);
    }

    @Override
    public void setApp(App app) {
        this.app = (WebApp) app;
    }
}
