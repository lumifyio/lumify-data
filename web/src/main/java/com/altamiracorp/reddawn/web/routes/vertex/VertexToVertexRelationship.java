package com.altamiracorp.reddawn.web.routes.vertex;

import com.altamiracorp.reddawn.RedDawnSession;
import com.altamiracorp.reddawn.model.graph.GraphVertex;
import com.altamiracorp.reddawn.model.graph.GraphRepository;
import com.altamiracorp.reddawn.web.Responder;
import com.altamiracorp.reddawn.web.WebApp;
import com.altamiracorp.web.App;
import com.altamiracorp.web.AppAware;
import com.altamiracorp.web.Handler;
import com.altamiracorp.web.HandlerChain;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

public class VertexToVertexRelationship implements Handler, AppAware {
    private GraphRepository graphRepository = new GraphRepository();
    private WebApp app;

    @Override
    public void setApp(App app) {
        this.app = (WebApp) app;
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, HandlerChain chain) throws Exception {
        RedDawnSession session = app.getRedDawnSession(request);
        String source = request.getParameter("source");
        String target = request.getParameter("target");
        String label = request.getParameter("label");

        HashMap<String, String> properties = graphRepository.getEdgeProperties(session.getGraphSession(), source, target, label);
        GraphVertex sourceVertex = graphRepository.findVertex(session.getGraphSession(), source);
        GraphVertex targetVertex = graphRepository.findVertex(session.getGraphSession(), target);

        JSONObject results = new JSONObject();
        results = resultsToJson(source, "source", sourceVertex, results);
        results = resultsToJson(target, "target", targetVertex, results);

        JSONArray propertyJson = new JSONArray();
        for (Map.Entry<String, String> p : properties.entrySet()) {
            JSONObject property = new JSONObject();
            property.put("key", p.getKey());
            property.put("value", p.getValue());
            propertyJson.put(property);
        }
        results.put("properties", propertyJson);

        new Responder(response).respondWith(results);
    }

    private JSONObject resultsToJson(String id, String prefix, GraphVertex graphVertex, JSONObject obj) throws JSONException {
        if (graphVertex.getProperty("_rowKey") == null){
            obj.put(prefix + "RowKey", "");
        } else {
            obj.put(prefix + "RowKey", graphVertex.getProperty("_rowKey"));
        }

        obj.put(prefix + "Id", id);
        obj.put(prefix + "SubType", graphVertex.getProperty("subType"));

        if (graphVertex.getProperty("title") != "") {
            obj.put(prefix + "Title", graphVertex.getProperty("title"));
        } else {
            obj.put(prefix + "Title", graphVertex.getProperty("type"));
        }

        obj.put(prefix + "Type", graphVertex.getProperty("type"));
        return obj;
    }
}
