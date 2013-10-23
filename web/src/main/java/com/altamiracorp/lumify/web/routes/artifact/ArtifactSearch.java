package com.altamiracorp.lumify.web.routes.artifact;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.altamiracorp.lumify.core.user.User;
import com.altamiracorp.lumify.core.model.graph.GraphVertex;
import com.altamiracorp.lumify.core.model.ontology.OntologyRepository;
import com.altamiracorp.lumify.core.model.artifact.ArtifactRepository;
import com.altamiracorp.lumify.web.BaseRequestHandler;
import com.altamiracorp.miniweb.HandlerChain;
import com.google.inject.Inject;

public class ArtifactSearch extends BaseRequestHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(ArtifactSearch.class);

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
        LOGGER.info("Number of artifacts returned for query: " + vertices.size());

        JSONObject results = new JSONObject();
        results.put("vertices", GraphVertex.toJson(vertices));

        respondWithJson(response, results);
    }
}
