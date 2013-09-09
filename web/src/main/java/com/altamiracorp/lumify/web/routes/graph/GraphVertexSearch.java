package com.altamiracorp.lumify.web.routes.graph;

import com.altamiracorp.lumify.AppSession;
import com.altamiracorp.lumify.model.graph.GraphRepository;
import com.altamiracorp.lumify.model.graph.GraphVertex;
import com.altamiracorp.lumify.model.ontology.OntologyRepository;
import com.altamiracorp.lumify.model.ontology.Property;
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
import java.util.List;

public class GraphVertexSearch implements Handler, AppAware {
    private GraphRepository graphRepository = new GraphRepository();
    private OntologyRepository ontologyRepository = new OntologyRepository();
    private WebApp app;

    @Override
    public void setApp(App app) {
        this.app = (WebApp) app;
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, HandlerChain chain) throws Exception {
        AppSession session = app.getAppSession(request);
        String query = request.getParameter("q");
        String filter = request.getParameter("filter");
        JSONArray filterJson = new JSONArray(filter);

        resolvePropertyIds(session, filterJson);

        List<GraphVertex> vertices = graphRepository.searchVerticesByTitle(session.getGraphSession(), query, filterJson);
        JSONObject results = new JSONObject();
        results.put("vertices", GraphVertex.toJson(vertices));
        new Responder(response).respondWith(results);
    }

    private void resolvePropertyIds(AppSession session, JSONArray filterJson) throws JSONException {
        for (int i = 0; i < filterJson.length(); i++) {
            JSONObject filter = filterJson.getJSONObject(i);
            if (filter.has("propertyId") && !filter.has("propertyName")) {
                int propertyId = filter.getInt("propertyId");
                Property property = ontologyRepository.getPropertyById(session.getGraphSession(), propertyId);
                if (property == null) {
                    throw new RuntimeException("Could not find property with id: " + propertyId);
                }
                filter.put("propertyName", property.getTitle());
                filter.put("propertyDataType", property.getDataType());
            }
        }
    }
}
