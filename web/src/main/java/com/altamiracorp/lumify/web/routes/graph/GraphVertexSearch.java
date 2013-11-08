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
        // FIXME: pass offset/size/subType to vertex search
        List<GraphVertex> vertices = graphRepository.searchVerticesByTitle(query, filterJson, user);
        LOGGER.info("Number of vertices returned for query: " + vertices.size());

        JSONObject results = new JSONObject();
        results.put("vertices", GraphVertex.toJson(vertices));

        // TODO: add verticesCount with count of all results (for pagination)
        results.put("verticesCount", 0);

        respondWithJson(response, results);
    }
}
