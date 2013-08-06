package com.altamiracorp.reddawn.web.routes.graph;

import com.altamiracorp.reddawn.RedDawnSession;
import com.altamiracorp.reddawn.model.graph.GraphRepository;
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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class GraphNodeToGraphNodeRelationship implements Handler, AppAware {
    private GraphRepository graphRepository = new GraphRepository();
    private WebApp app;

    @Override
    public void setApp (App app) {
        this.app = (WebApp) app;
    }

    @Override
    public void handle (HttpServletRequest request, HttpServletResponse response, HandlerChain chain) throws Exception {
        RedDawnSession session = app.getRedDawnSession(request);
        String source = request.getParameter("source");
        String target = request.getParameter("target");

        HashMap<String, String> properties = graphRepository.getEdgeProperties(session.getGraphSession(), source, target);

        JSONObject results = new JSONObject();
        results.put("source", graphRepository.findTitle(session.getGraphSession(),source));
        results.put("target", graphRepository.findTitle(session.getGraphSession(),target));
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
}
