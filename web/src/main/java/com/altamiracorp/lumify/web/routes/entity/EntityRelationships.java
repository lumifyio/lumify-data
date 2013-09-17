package com.altamiracorp.lumify.web.routes.entity;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONObject;

import com.altamiracorp.lumify.AppSession;
import com.altamiracorp.lumify.model.graph.GraphRelationship;
import com.altamiracorp.lumify.model.graph.GraphRepository;
import com.altamiracorp.lumify.web.BaseRequestHandler;
import com.altamiracorp.web.HandlerChain;
import com.google.inject.Inject;

public class EntityRelationships extends BaseRequestHandler {
    private final GraphRepository graphRepository;

    @Inject
    public EntityRelationships(final GraphRepository repo) {
        graphRepository = repo;
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, HandlerChain chain) throws Exception {
        AppSession session = app.getAppSession(request);

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

        respondWithJson(response, resultsJson);
    }
}
