package com.altamiracorp.lumify.web.routes.ontology;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.altamiracorp.lumify.AppSession;
import com.altamiracorp.lumify.model.ontology.OntologyRepository;
import com.altamiracorp.lumify.model.ontology.Relationship;
import com.altamiracorp.lumify.web.BaseRequestHandler;
import com.altamiracorp.web.HandlerChain;
import com.google.inject.Inject;

public class RelationshipLabelList extends BaseRequestHandler {
    private final OntologyRepository ontologyRepository;

    @Inject
    public RelationshipLabelList(final OntologyRepository repo) {
        ontologyRepository = repo;
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, HandlerChain chain) throws Exception {
        final String sourceConceptTypeId = getOptionalParameter(request, "sourceConceptTypeId");
        final String destConceptTypeId = getOptionalParameter(request, "destConceptTypeId");

        AppSession session = app.getAppSession(request);

        if (destConceptTypeId == null) {
            List<Relationship> relationships = ontologyRepository.getRelationshipLabels(session.getGraphSession());

            JSONObject json = new JSONObject();
            json.put("relationships", Relationship.toJsonRelationships(relationships));

            respondWithJson(response, json);
        } else {
            List<Relationship> relationships = ontologyRepository.getRelationships(session.getGraphSession(), sourceConceptTypeId, destConceptTypeId);

            JSONObject result = new JSONObject();
            JSONArray relationshipsJson = new JSONArray();
            for (Relationship relationship : relationships) {
                relationshipsJson.put(toJson(relationship));
            }
            result.put("relationships", relationshipsJson);

            respondWithJson(response, result);
        }
    }

    private JSONObject toJson(Relationship relationship) throws JSONException {
        JSONObject json = new JSONObject();
        json.put("id", relationship.getId());
        json.put("title", relationship.getTitle());
        return json;
    }
}
