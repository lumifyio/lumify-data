package com.altamiracorp.reddawn.web.routes.entity;

import com.altamiracorp.reddawn.web.WebApp;
import com.altamiracorp.web.App;
import com.altamiracorp.web.AppAware;
import com.altamiracorp.web.Handler;
import com.altamiracorp.web.HandlerChain;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class EntityRelationships implements Handler, AppAware {
    private WebApp app;

    @Override
    public void setApp(App app) {
        this.app = (WebApp) app;
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, HandlerChain chain) throws Exception {
        String[] entityIds = request.getParameterValues("entityIds[]");
        if (entityIds == null) {
            entityIds = new String[0];
        }

        String[] artifactIds = request.getParameterValues("artifactIds[]");
        if (artifactIds == null) {
            artifactIds = new String[0];
        }

        // TODO load the relationships from the database
        JSONArray resultsJson = new JSONArray();
        for (String entityId : entityIds) {
            for (String e : entityIds) {
                JSONObject rel = new JSONObject();
                rel.put("from", entityId);
                rel.put("to", e);
                resultsJson.put(rel);
            }

            for (String a : artifactIds) {
                JSONObject rel = new JSONObject();
                rel.put("from", entityId);
                rel.put("to", a);
                resultsJson.put(rel);
            }
        }

        response.setContentType("application/json");
        response.getWriter().write(resultsJson.toString());
    }
}
