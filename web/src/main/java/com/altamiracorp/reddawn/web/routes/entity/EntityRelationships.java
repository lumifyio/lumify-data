package com.altamiracorp.reddawn.web.routes.entity;

import com.altamiracorp.reddawn.RedDawnSession;
import com.altamiracorp.reddawn.model.Row;
import com.altamiracorp.reddawn.model.Session;
import com.altamiracorp.reddawn.model.graph.GraphNode;
import com.altamiracorp.reddawn.model.graph.GraphRepository;
import com.altamiracorp.reddawn.ucd.artifactTermIndex.ArtifactTermIndex;
import com.altamiracorp.reddawn.ucd.artifactTermIndex.ArtifactTermIndexRepository;
import com.altamiracorp.reddawn.ucd.statement.Statement;
import com.altamiracorp.reddawn.ucd.statement.StatementRepository;
import com.altamiracorp.reddawn.ucd.statement.StatementRowKey;
import com.altamiracorp.reddawn.ucd.term.TermRowKey;
import com.altamiracorp.reddawn.web.Responder;
import com.altamiracorp.reddawn.web.WebApp;
import com.altamiracorp.web.App;
import com.altamiracorp.web.AppAware;
import com.altamiracorp.web.Handler;
import com.altamiracorp.web.HandlerChain;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.rexster.GraphResource;
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
        RedDawnSession session = this.app.getRedDawnSession(request);

        JSONObject jsonArray = new JSONObject(request.getParameter("json"));
        JSONArray entityIds = jsonArray.getJSONArray("entityIds");
        JSONArray artifactIds = jsonArray.getJSONArray("artifactIds");

        List<String> allIds = new ArrayList<String>();
        List<String> artifactGraphNodeIds = new ArrayList<String>();
        List<String> entityGraphNodeIds = new ArrayList<String>();

        for (int i = 0; i < entityIds.length(); i++) {
            allIds.add(entityIds.getString(i));
            entityGraphNodeIds.add(entityIds.getString(i));
        }
        for (int i = 0; i < artifactIds.length(); i++) {
            allIds.add(artifactIds.getString(i));
            artifactGraphNodeIds.add(artifactIds.getString(i));
        }

        JSONArray resultsJson = new JSONArray();

        HashMap<String, HashSet<String>> relationships = graphRepository.getRelationships(session.getGraphSession(),allIds);

        for (Map.Entry<String, HashSet<String>> relationship : relationships.entrySet()){
            for (String toId : relationship.getValue()) {
                JSONObject rel = new JSONObject();
                if (artifactGraphNodeIds.contains(relationship.getKey())){
                    rel.put("relationshipType", "artifactToEntity");
                } else if (entityGraphNodeIds.contains(relationship.getKey())) {
                    rel.put("relationshipType", "entityToEntity");
                }
                rel.put("from", relationship.getKey());
                rel.put("to", toId);
                resultsJson.put(rel);
            }
        }

        new Responder(response).respondWith(resultsJson);
    }
}
