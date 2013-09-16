package com.altamiracorp.lumify.web.routes.vertex;

import com.altamiracorp.lumify.AppSession;
import com.altamiracorp.lumify.model.graph.GraphRepository;
import com.altamiracorp.lumify.model.graph.GraphVertex;
import com.altamiracorp.lumify.model.ontology.OntologyRepository;
import com.altamiracorp.lumify.web.BaseRequestHandler;
import com.altamiracorp.web.HandlerChain;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

public class VertexToVertexRelationship extends BaseRequestHandler {
    private GraphRepository graphRepository = new GraphRepository();
    private OntologyRepository ontologyRepository = new OntologyRepository();

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, HandlerChain chain) throws Exception {
        final String source = getRequiredParameter(request, "source");
        final String target = getRequiredParameter(request, "target");
        final String label = getRequiredParameter(request, "label");

        AppSession session = app.getAppSession(request);
        Map<String, String> properties = graphRepository.getEdgeProperties(session.getGraphSession(), source, target, label);
        GraphVertex sourceVertex = graphRepository.findVertex(session.getGraphSession(), source);
        GraphVertex targetVertex = graphRepository.findVertex(session.getGraphSession(), target);

        JSONObject results = new JSONObject();
        results = resultsToJson(source, "source", sourceVertex, results);
        results = resultsToJson(target, "target", targetVertex, results);

        JSONArray propertyJson = new JSONArray();
        for (Map.Entry<String, String> p : properties.entrySet()) {
            JSONObject property = new JSONObject();
            property.put("key", p.getKey());
            String displayName = ontologyRepository.getDisplayNameForLabel(session.getGraphSession(), p.getValue());
            if (displayName == null) {
                property.put("value", p.getValue());
            } else {
                property.put("value", displayName);
            }
            propertyJson.put(property);
        }
        results.put("properties", propertyJson);

        respondWithJson(response, results);
    }

    private JSONObject resultsToJson(String id, String prefix, GraphVertex graphVertex, JSONObject obj) throws JSONException {
        if (graphVertex.getProperty("_rowKey") == null) {
            obj.put(prefix + "RowKey", "");
        } else {
            obj.put(prefix + "RowKey", graphVertex.getProperty("_rowKey"));
        }

        obj.put(prefix + "Id", id);
        obj.put(prefix + "SubType", graphVertex.getProperty("_subType"));

        if (graphVertex.getProperty("title") != "") {
            obj.put(prefix + "Title", graphVertex.getProperty("title"));
        } else {
            obj.put(prefix + "Title", graphVertex.getProperty("_type").toString().toLowerCase());
        }

        obj.put(prefix + "Type", graphVertex.getProperty("_type").toString().toLowerCase());
        return obj;
    }
}
