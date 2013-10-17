package com.altamiracorp.lumify.web.routes.graph;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.altamiracorp.lumify.core.user.User;
import com.altamiracorp.lumify.core.model.graph.GraphRepository;
import com.altamiracorp.lumify.core.model.graph.GraphVertex;
import com.altamiracorp.lumify.core.model.ontology.OntologyRepository;
import com.altamiracorp.lumify.web.BaseRequestHandler;
import com.altamiracorp.web.HandlerChain;
import com.google.inject.Inject;

public class GraphVertexSearch extends BaseRequestHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(GraphVertexSearch.class);

    private final GraphRepository graphRepository;
    private final OntologyRepository ontologyRepository;

    @Inject
    public GraphVertexSearch(final OntologyRepository ontologyRepo, final GraphRepository graphRepo) {
        ontologyRepository = ontologyRepo;
        graphRepository = graphRepo;
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, HandlerChain chain) throws Exception {
        final String query = getRequiredParameter(request, "q");
        final String filter = getRequiredParameter(request, "filter");

        User user = getUser(request);
        JSONArray filterJson = new JSONArray(filter);

        ontologyRepository.resolvePropertyIds(filterJson, user);

        graphRepository.commit();
        List<GraphVertex> vertices = graphRepository.searchVerticesByTitle(query, filterJson, user);
        LOGGER.info("Number of vertices returned for query: " + vertices.size());

        JSONObject results = new JSONObject();
        results.put("vertices", GraphVertex.toJson(vertices));

        respondWithJson(response, results);
    }
}
