package com.altamiracorp.lumify.web.routes.vertex;

import com.altamiracorp.lumify.AppSession;
import com.altamiracorp.lumify.model.graph.GraphRelationship;
import com.altamiracorp.lumify.model.graph.GraphRepository;
import com.altamiracorp.lumify.model.graph.GraphVertex;
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
        AppSession session = this.app.getAppSession(request);

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
