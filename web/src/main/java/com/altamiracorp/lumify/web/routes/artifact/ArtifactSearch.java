package com.altamiracorp.lumify.web.routes.artifact;

import com.altamiracorp.lumify.core.user.User;
import com.altamiracorp.lumify.model.graph.GraphVertex;
import com.altamiracorp.lumify.model.ontology.OntologyRepository;
import com.altamiracorp.lumify.ucd.artifact.ArtifactRepository;
import com.altamiracorp.lumify.web.BaseRequestHandler;
import com.altamiracorp.web.HandlerChain;
import com.google.inject.Inject;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

public class ArtifactSearch extends BaseRequestHandler {
    private final ArtifactRepository artifactRepository;
    private final OntologyRepository ontologyRepository;

    @Inject
    public ArtifactSearch(
            ArtifactRepository artifactRepository,
            OntologyRepository ontologyRepository) {
        this.artifactRepository = artifactRepository;
        this.ontologyRepository = ontologyRepository;
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, HandlerChain chain) throws Exception {
        final String query = getRequiredParameter(request, "q");
        final String filter = getRequiredParameter(request, "filter");

        User user = getUser(request);
        JSONArray filterJson = new JSONArray(filter);

        ontologyRepository.resolvePropertyIds(filterJson, user);

        List<GraphVertex> vertices = artifactRepository.search(query, filterJson, user);
        JSONObject results = new JSONObject();
        results.put("vertices", GraphVertex.toJson(vertices));

        respondWithJson(response, results);
    }
}
