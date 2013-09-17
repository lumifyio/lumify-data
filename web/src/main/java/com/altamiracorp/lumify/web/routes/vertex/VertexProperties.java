package com.altamiracorp.lumify.web.routes.vertex;

import com.altamiracorp.lumify.AppSession;
import com.altamiracorp.lumify.model.graph.GraphRepository;
import com.altamiracorp.lumify.web.BaseRequestHandler;
import com.altamiracorp.web.HandlerChain;
import org.json.JSONException;
import org.json.JSONObject;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

public class VertexProperties extends BaseRequestHandler {
    private GraphRepository graphRepository = new GraphRepository();

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, HandlerChain chain) throws Exception {
        final String graphVertexId = getAttributeString(request, "graphVertexId");
        AppSession session = app.getAppSession(request);

        Map<String, String> properties = graphRepository.getVertexProperties(session.getGraphSession(), graphVertexId);
        JSONObject propertiesJson = propertiesToJson(properties);

        JSONObject json = new JSONObject();
        json.put("id", graphVertexId);
        json.put("properties", propertiesJson);

        respondWithJson(response, json);
    }

    public static JSONObject propertiesToJson(Map<String, String> properties) throws JSONException {
        JSONObject resultsJson = new JSONObject();
        for (Map.Entry<String, String> property : properties.entrySet()) {
            resultsJson.put(property.getKey(), property.getValue());
        }
        return resultsJson;
    }
}
