package com.altamiracorp.reddawn.web.routes.vertex;

import com.altamiracorp.reddawn.RedDawnSession;
import com.altamiracorp.reddawn.model.graph.GraphRepository;
import com.altamiracorp.reddawn.model.ontology.OntologyRepository;
import com.altamiracorp.reddawn.model.ontology.Property;
import com.altamiracorp.reddawn.web.Messaging;
import com.altamiracorp.reddawn.web.Responder;
import com.altamiracorp.reddawn.web.WebApp;
import com.altamiracorp.web.App;
import com.altamiracorp.web.AppAware;
import com.altamiracorp.web.Handler;
import com.altamiracorp.web.HandlerChain;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

public class VertexSetProperty implements Handler, AppAware {
    private static final Logger LOGGER = LoggerFactory.getLogger(VertexSetProperty.class.getName());
    private WebApp app;
    private GraphRepository graphRepository = new GraphRepository();
    private OntologyRepository ontologyRepository = new OntologyRepository();

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, HandlerChain chain) throws Exception {
        RedDawnSession session = app.getRedDawnSession(request);
        String graphVertexId = (String) request.getAttribute("graphVertexId");
        String propertyName = request.getParameter("propertyName");
        String valueStr = request.getParameter("value");

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

        graphRepository.setPropertyVertex(session.getGraphSession(), graphVertexId, propertyName, value);

        Messaging.broadcastPropertyChange(graphVertexId, propertyName, value);

        Map<String, String> properties = graphRepository.getVertexProperties(session.getGraphSession(), graphVertexId);
        JSONArray propertiesJson = VertexProperties.propertiesToJson(properties);
        JSONObject json = new JSONObject();
        json.put("properties", propertiesJson);
        new Responder(response).respondWith(json);
    }

    @Override
    public void setApp(App app) {
        this.app = (WebApp) app;
    }
}
