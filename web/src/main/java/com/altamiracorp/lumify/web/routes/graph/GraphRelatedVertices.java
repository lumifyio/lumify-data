package com.altamiracorp.lumify.web.routes.graph;

import com.altamiracorp.lumify.AppSession;
import com.altamiracorp.lumify.model.graph.GraphRepository;
import com.altamiracorp.lumify.model.graph.GraphVertex;
import com.altamiracorp.lumify.model.ontology.Concept;
import com.altamiracorp.lumify.model.ontology.OntologyRepository;
import com.altamiracorp.lumify.model.ontology.PropertyName;
import com.altamiracorp.lumify.model.ontology.VertexType;
import com.altamiracorp.lumify.web.Responder;
import com.altamiracorp.lumify.web.WebApp;
import com.altamiracorp.web.App;
import com.altamiracorp.web.AppAware;
import com.altamiracorp.web.Handler;
import com.altamiracorp.web.HandlerChain;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

public class GraphRelatedVertices implements Handler, AppAware {
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
        String graphVertexId = (String) request.getAttribute("graphVertexId");
        String limitParentConceptId = request.getParameter("limitParentConceptId");
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
        new Responder(response).respondWith(json);

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

