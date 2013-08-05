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

        String [] ids = request.getParameterValues("ids[]");
        if (ids == null) {
            ids = new String [0];
        }

        List<String> allIds = new ArrayList<String>();

        for (int i = 0; i < ids.length; i++) {
            allIds.add(ids[i]);
        }

        JSONArray resultsJson = new JSONArray();

        HashMap<String, HashSet<String>> relationships = graphRepository.getRelationships(session.getGraphSession(),allIds);

        for (Map.Entry<String, HashSet<String>> relationship : relationships.entrySet()){
            for (String toId : relationship.getValue()) {
                JSONObject rel = new JSONObject();
                String type = graphRepository.getNodeType(session.getGraphSession(), relationship.getKey());
                if (type.equals("artifact")) {
                    rel.put("relationshipType", "artifactToEntity");
                } else {
                    rel.put ("relationshipType", "entityToEntity");
                }

                rel.put("from", relationship.getKey());
                rel.put("to", toId);
                resultsJson.put(rel);
            }
        }

        new Responder(response).respondWith(resultsJson);
    }
}
