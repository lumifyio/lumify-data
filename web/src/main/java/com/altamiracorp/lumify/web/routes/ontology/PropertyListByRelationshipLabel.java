package com.altamiracorp.lumify.web.routes.ontology;

import com.altamiracorp.lumify.AppSession;
import com.altamiracorp.lumify.model.ontology.OntologyRepository;
import com.altamiracorp.lumify.model.ontology.Property;
import com.altamiracorp.lumify.web.BaseRequestHandler;
import com.altamiracorp.web.HandlerChain;
import org.json.JSONObject;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

public class PropertyListByRelationshipLabel extends BaseRequestHandler {
    private OntologyRepository ontologyRepository = new OntologyRepository();

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, HandlerChain chain) throws Exception {
        final String relationshipLabel = getAttributeString(request, "relationshipLabel");
        AppSession session = app.getAppSession(request);

        List<Property> properties = ontologyRepository.getPropertiesByRelationship(session.getGraphSession(), relationshipLabel);

        JSONObject json = new JSONObject();
        json.put("properties", Property.toJsonProperties(properties));

        respondWithJson(response, json);
    }
}
