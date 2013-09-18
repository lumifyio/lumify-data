package com.altamiracorp.lumify.web.routes.entity;

import com.altamiracorp.lumify.core.user.User;
import com.altamiracorp.lumify.model.graph.GraphRepository;
import com.altamiracorp.lumify.model.graph.GraphVertex;
import com.altamiracorp.lumify.model.ontology.VertexType;
import com.altamiracorp.lumify.web.BaseRequestHandler;
import com.altamiracorp.web.HandlerChain;
import com.google.inject.Inject;
import org.json.JSONObject;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

public class EntitySearch extends BaseRequestHandler {
    private final GraphRepository graphRepository;

    @Inject
    public EntitySearch(final GraphRepository repo) {
        graphRepository = repo;
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, HandlerChain chain) throws Exception {
        User user = getUser(request);
        final String query = getRequiredParameter(request, "q");

        List<GraphVertex> vertices = graphRepository.searchVerticesByTitleAndType(query, VertexType.ENTITY, user);

        JSONObject results = new JSONObject();
        results.put("vertices", GraphVertex.toJson(vertices));

        respondWithJson(response, results);
    }
}
