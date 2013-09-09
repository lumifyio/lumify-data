package com.altamiracorp.reddawn.web.routes.vertex;

import com.altamiracorp.reddawn.RedDawnSession;
import com.altamiracorp.reddawn.model.graph.GraphRelationship;
import com.altamiracorp.reddawn.model.graph.GraphRepository;
import com.altamiracorp.reddawn.model.graph.GraphVertex;
import com.altamiracorp.reddawn.web.Responder;
import com.altamiracorp.reddawn.web.WebApp;
import com.altamiracorp.web.App;
import com.altamiracorp.web.AppAware;
import com.altamiracorp.web.Handler;
import com.altamiracorp.web.HandlerChain;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

public class VertexRelationships implements Handler, AppAware {
    private WebApp app;
    private GraphRepository graphRepository = new GraphRepository();

    @Override
    public void setApp(App app) {
        this.app = (WebApp) app;
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, HandlerChain chain) throws Exception {
        RedDawnSession session = this.app.getRedDawnSession(request);

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

        new Responder(response).respondWith(json);
    }
}
