package com.altamiracorp.lumify.web.routes.vertex;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.altamiracorp.lumify.AppSession;
import com.altamiracorp.lumify.model.graph.GraphRepository;
import com.altamiracorp.lumify.web.BaseRequestHandler;
import com.altamiracorp.web.HandlerChain;

public class VertexProperties extends BaseRequestHandler {
    private GraphRepository graphRepository = new GraphRepository();

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, HandlerChain chain) throws Exception {
        final String graphVertexId = getAttributeString(request, "graphVertexId");
        AppSession session = app.getAppSession(request);

        Map<String, String> properties = graphRepository.getVertexProperties(session.getGraphSession(), graphVertexId);
        JSONArray resultsJson = propertiesToJson(properties);

        respondWithJson(response, resultsJson);
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
