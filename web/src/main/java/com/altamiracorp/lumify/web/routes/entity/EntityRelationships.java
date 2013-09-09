package com.altamiracorp.lumify.web.routes.entity;

import com.altamiracorp.lumify.AppSession;
import com.altamiracorp.lumify.model.graph.GraphRelationship;
import com.altamiracorp.lumify.model.graph.GraphRepository;
import com.altamiracorp.lumify.web.Responder;
import com.altamiracorp.lumify.web.WebApp;
import com.altamiracorp.web.App;
import com.altamiracorp.web.AppAware;
import com.altamiracorp.web.Handler;
import com.altamiracorp.web.HandlerChain;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

public class EntityRelationships implements Handler, AppAware {
    private WebApp app;
    private GraphRepository graphRepository = new GraphRepository();

    @Override
    public void setApp(App app) {
        this.app = (WebApp) app;
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, HandlerChain chain) throws Exception {
        AppSession session = this.app.getAppSession(request);

        String[] ids = request.getParameterValues("ids[]");
        if (ids == null) {
            ids = new String[0];
        }

        List<String> allIds = new ArrayList<String>();

        for (int i = 0; i < ids.length; i++) {
            allIds.add(ids[i]);
        }

        JSONArray resultsJson = new JSONArray();

        List<GraphRelationship> graphRelationships = graphRepository.getRelationships(session.getGraphSession(), allIds);

        for (GraphRelationship graphRelationship : graphRelationships) {
                JSONObject rel = new JSONObject();
                rel.put("from", graphRelationship.getSourceVertexId());
                rel.put("to", graphRelationship.getDestVertexId());
                rel.put("relationshipType", graphRelationship.getLabel());
                resultsJson.put(rel);
        }

        new Responder(response).respondWith(resultsJson);
    }
}
