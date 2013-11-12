package com.altamiracorp.lumify.web.routes.graph;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.altamiracorp.lumify.core.model.graph.GraphPagedResults;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.altamiracorp.lumify.core.user.User;
import com.altamiracorp.lumify.core.model.graph.GraphRepository;
import com.altamiracorp.lumify.core.model.graph.GraphVertex;
import com.altamiracorp.lumify.core.model.ontology.OntologyRepository;
import com.altamiracorp.lumify.web.BaseRequestHandler;
import com.altamiracorp.miniweb.HandlerChain;
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
        final long offset = getOptionalParameterLong(request, "offset", 0);
        final long size = getOptionalParameterLong(request, "size", 100);
        final String subType = getOptionalParameter(request, "subType");

        User user = getUser(request);
        JSONArray filterJson = new JSONArray(filter);

        ontologyRepository.resolvePropertyIds(filterJson, user);

        graphRepository.commit();

        GraphPagedResults pagedResults = graphRepository.searchVerticesByTitle(query, filterJson, user, offset, size, subType);

        JSONArray vertices = new JSONArray();
        JSONObject counts = new JSONObject();
        for (Map.Entry<String, List<GraphVertex>> entry : pagedResults.getResults().entrySet()) {
            vertices.put(GraphVertex.toJson(entry.getValue()));
            counts.put(entry.getKey(), pagedResults.getCount().get(entry.getKey()));
        }
        LOGGER.info("Number of vertices returned for query: " + pagedResults.getResults().size());

        JSONObject results = new JSONObject();
        results.put("vertices", vertices);
        results.put("verticesCount", counts);

        respondWithJson(response, results);
    }
}
