package com.altamiracorp.reddawn.web.routes.vertex;

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
import java.util.Map;

public class VertexProperties implements Handler, AppAware {
    private WebApp app;
    private GraphRepository graphRepository = new GraphRepository();

    @Override
    public void setApp(App app) {
        this.app = (WebApp) app;
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, HandlerChain chain) throws Exception {
        RedDawnSession session = this.app.getRedDawnSession(request);

        Map<String, String> properties = graphRepository.getProperties(session.getGraphSession(), (String) request.getAttribute("graphVertexId"));

        JSONArray resultsJson = new JSONArray();
        for(Map.Entry<String, String> property : properties.entrySet()) {
            JSONObject propertyJson = new JSONObject();
            propertyJson.put("key", property.getKey());
            propertyJson.put("value", property.getValue());
            resultsJson.put(propertyJson);
        }

        new Responder(response).respondWith(resultsJson);
    }
}
