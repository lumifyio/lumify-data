package com.altamiracorp.lumify.web.routes.vertex;

import com.altamiracorp.lumify.core.user.User;
import com.altamiracorp.lumify.core.model.graph.GraphRepository;
import com.altamiracorp.lumify.core.model.graph.GraphVertex;
import com.altamiracorp.lumify.web.BaseRequestHandler;
import com.altamiracorp.web.HandlerChain;
import com.google.inject.Inject;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

public class VertexMultiple extends BaseRequestHandler {
    private final GraphRepository graphRepository;

    @Inject
    public VertexMultiple(final GraphRepository repo) {
        graphRepository = repo;
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, HandlerChain chain) throws Exception {
        String[] vertexIds = request.getParameterValues("vertexIds[]");
        User user = getUser(request);

        List<GraphVertex> graphVertices = graphRepository.findVertices(vertexIds, user);

        respondWithJson(response, GraphVertex.toJson(graphVertices));
    }
}
