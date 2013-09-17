package com.altamiracorp.lumify.web.routes.vertex;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.altamiracorp.lumify.AppSession;
import com.altamiracorp.lumify.model.graph.GraphRepository;
import com.altamiracorp.lumify.model.graph.GraphVertex;
import com.altamiracorp.lumify.web.BaseRequestHandler;
import com.altamiracorp.web.HandlerChain;
import com.google.inject.Inject;

public class VertexMultiple extends BaseRequestHandler {
    private final GraphRepository graphRepository;

    @Inject
    public VertexMultiple(final GraphRepository repo) {
        graphRepository = repo;
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, HandlerChain chain) throws Exception {
        String[] vertexIds = request.getParameterValues("vertexIds[]");
        AppSession session = app.getAppSession(request);

        List<GraphVertex> graphVertices = graphRepository.findVertices(session.getGraphSession(), vertexIds);

        respondWithJson(response, GraphVertex.toJson(graphVertices));
    }
}
