package com.altamiracorp.lumify.web.routes.graph;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONObject;

import com.altamiracorp.lumify.AppSession;
import com.altamiracorp.lumify.model.graph.GraphRepository;
import com.altamiracorp.lumify.model.graph.GraphVertex;
import com.altamiracorp.lumify.model.ontology.Concept;
import com.altamiracorp.lumify.model.ontology.OntologyRepository;
import com.altamiracorp.lumify.model.ontology.PropertyName;
import com.altamiracorp.lumify.model.ontology.VertexType;
import com.altamiracorp.lumify.web.BaseRequestHandler;
import com.altamiracorp.web.HandlerChain;
import com.google.inject.Inject;

public class GraphRelatedVertices extends BaseRequestHandler {
    private GraphRepository graphRepository = new GraphRepository();
    private final OntologyRepository ontologyRepository;

    @Inject
    public GraphRelatedVertices(final OntologyRepository repo) {
        ontologyRepository = repo;
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, HandlerChain chain) throws Exception {
        String graphVertexId = getAttributeString(request, "graphVertexId");
        String limitParentConceptId = getOptionalParameter(request, "limitParentConceptId");

        AppSession session = app.getAppSession(request);
        List<Concept> limitConcepts = null;

        if (limitParentConceptId != null) {
            limitConcepts = ontologyRepository.getConceptByIdAndChildren(session.getGraphSession(), limitParentConceptId);
            if (limitConcepts == null) {
                throw new RuntimeException("Bad 'limitParentConceptId', no concept found for id: " + limitParentConceptId);
            }
        }

        List<GraphVertex> graphVertices = graphRepository.getRelatedVertices(session.getGraphSession(), graphVertexId);

        JSONObject json = new JSONObject();
        JSONArray verticesJson = new JSONArray();
        for (GraphVertex graphVertex : graphVertices) {
            if (limitConcepts != null && isLimited(limitConcepts, graphVertex)) {
                continue;
            }
            JSONObject graphVertexJson = graphVertex.toJson();
            verticesJson.put(graphVertexJson);
        }
        json.put("vertices", verticesJson);

        respondWithJson(response, json);

        chain.next(request, response);
    }

    private boolean isLimited(List<Concept> limitConcepts, GraphVertex graphVertex) {
        String type = (String) graphVertex.getProperty(PropertyName.TYPE);
        if (type.equals(VertexType.ENTITY.toString())) {
            String conceptId = (String) graphVertex.getProperty(PropertyName.SUBTYPE);
            for (Concept concept : limitConcepts) {
                if (concept.getId().equals(conceptId)) {
                    return false;
                }
            }
        } else if (type.equals(VertexType.ARTIFACT.toString())) {
            for (Concept concept : limitConcepts) {
                if (concept.getTitle().equals(VertexType.ARTIFACT.toString())) {
                    return false;
                }
            }
            return true;
        } else {
            return true;
        }

        return true;
    }
}

