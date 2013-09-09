package com.altamiracorp.lumify.web.routes.vertex;

import com.altamiracorp.lumify.AppSession;
import com.altamiracorp.lumify.model.graph.GraphRepository;
import com.altamiracorp.lumify.web.Responder;
import com.altamiracorp.lumify.web.WebApp;
import com.altamiracorp.web.App;
import com.altamiracorp.web.AppAware;
import com.altamiracorp.web.Handler;
import com.altamiracorp.web.HandlerChain;
import org.json.JSONArray;
import org.json.JSONException;
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
        AppSession session = this.app.getAppSession(request);
        String graphVertexId = (String) request.getAttribute("graphVertexId");

        Map<String, String> properties = graphRepository.getVertexProperties(session.getGraphSession(), graphVertexId);
        JSONArray resultsJson = propertiesToJson(properties);

        new Responder(response).respondWith(resultsJson);
    }

    public static JSONArray propertiesToJson(Map<String, String> properties) throws JSONException {
        JSONArray resultsJson = new JSONArray();
        for (Map.Entry<String, String> property : properties.entrySet()) {
            JSONObject propertyJson = new JSONObject();
            propertyJson.put("key", property.getKey());
            propertyJson.put("value", property.getValue());
            resultsJson.put(propertyJson);
        }
        return resultsJson;
    }
}
