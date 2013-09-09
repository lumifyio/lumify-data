package com.altamiracorp.lumify.web.routes.relationship;

import com.altamiracorp.lumify.AppSession;
import com.altamiracorp.lumify.model.graph.GraphRepository;
import com.altamiracorp.lumify.model.ontology.OntologyRepository;
import com.altamiracorp.lumify.model.ontology.Property;
import com.altamiracorp.lumify.web.Responder;
import com.altamiracorp.lumify.web.WebApp;
import com.altamiracorp.lumify.web.routes.vertex.VertexProperties;
import com.altamiracorp.web.App;
import com.altamiracorp.web.AppAware;
import com.altamiracorp.web.Handler;
import com.altamiracorp.web.HandlerChain;
import org.json.JSONArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

public class SetRelationshipProperty implements Handler, AppAware {
    private static final Logger LOGGER = LoggerFactory.getLogger(SetRelationshipProperty.class.getName());
    private WebApp app;
    private GraphRepository graphRepository = new GraphRepository();
    private OntologyRepository ontologyRepository = new OntologyRepository();

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, HandlerChain chain) throws Exception {
        AppSession session = app.getAppSession(request);
        String relationshipLabel = request.getParameter("relationshipLabel");
        String propertyName = request.getParameter("propertyName");
        String valueStr = request.getParameter("value");
        String sourceId = request.getParameter("source");
        String destId = request.getParameter("dest");

        Property property = ontologyRepository.getProperty(session.getGraphSession(), propertyName);
        if (property == null) {
            throw new RuntimeException("Could not find property: " + propertyName);
        }

        Object value;
        try {
            value = property.convertString(valueStr);
        } catch (Exception ex) {
            LOGGER.warn("Validation error propertyName: " + propertyName + ", valueStr: " + valueStr, ex);
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, ex.getMessage());
            return;
        }

        graphRepository.setPropertyEdge(session.getGraphSession(), sourceId, destId, relationshipLabel, propertyName, value);

        Map<String, String> properties = graphRepository.getEdgeProperties(session.getGraphSession(), sourceId, destId, relationshipLabel);
        JSONArray resultsJson = VertexProperties.propertiesToJson(properties);
        new Responder(response).respondWith(resultsJson);
    }

    @Override
    public void setApp(App app) {
        this.app = (WebApp) app;
    }
}
