package com.altamiracorp.lumify.web.routes.vertex;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONObject;

import com.altamiracorp.lumify.AppSession;
import com.altamiracorp.lumify.model.graph.GraphRelationship;
import com.altamiracorp.lumify.model.graph.GraphRepository;
import com.altamiracorp.lumify.model.graph.GraphVertex;
import com.altamiracorp.lumify.web.BaseRequestHandler;
import com.altamiracorp.web.HandlerChain;
import com.google.inject.Inject;

public class VertexRelationships extends BaseRequestHandler {
    private final GraphRepository graphRepository;

    @Inject
    public VertexRelationships(final GraphRepository repo) {
        graphRepository = repo;
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, HandlerChain chain) throws Exception {
        AppSession session = app.getAppSession(request);

        Map<GraphRelationship, GraphVertex> relationships = graphRepository.getRelationships(session.getGraphSession(), (String) request.getAttribute("graphVertexId"));

        JSONObject json = new JSONObject();
        JSONArray relationshipsJson = new JSONArray();
        for (Map.Entry<GraphRelationship, GraphVertex> relationship : relationships.entrySet()) {
            JSONObject relationshipJson = new JSONObject();
            relationshipJson.put("relationship", relationship.getKey().toJson());
            relationshipJson.put("vertex", relationship.getValue().toJson());
            relationshipsJson.put(relationshipJson);
        }
        json.put("relationships", relationshipsJson);

        respondWithJson(response, json);
    }
}
