package com.altamiracorp.lumify.web.routes.ontology;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONObject;

import com.altamiracorp.lumify.AppSession;
import com.altamiracorp.lumify.model.ontology.OntologyRepository;
import com.altamiracorp.lumify.model.ontology.Property;
import com.altamiracorp.lumify.web.BaseRequestHandler;
import com.altamiracorp.web.HandlerChain;
import com.google.inject.Inject;

public class PropertyListByConceptId extends BaseRequestHandler {
    private final OntologyRepository ontologyRepository;

    @Inject
    public PropertyListByConceptId(final OntologyRepository repo) {
        ontologyRepository = repo;
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, HandlerChain chain) throws Exception {
        final String conceptId = getAttributeString(request, "conceptId");
        AppSession session = app.getAppSession(request);

        List<Property> properties = ontologyRepository.getPropertiesByConceptId(session.getGraphSession(), conceptId);

        JSONObject json = new JSONObject();
        json.put("properties", Property.toJsonProperties(properties));

        respondWithJson(response, json);
    }
}
