package com.altamiracorp.lumify.web.routes.graph;

import com.altamiracorp.lumify.AppSession;
import com.altamiracorp.lumify.model.graph.GraphRepository;
import com.altamiracorp.lumify.model.graph.GraphVertex;
import com.altamiracorp.lumify.web.BaseRequestHandler;
import com.altamiracorp.web.HandlerChain;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class GraphGetVertex extends BaseRequestHandler {
    private GraphRepository graphRepository = new GraphRepository();

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, HandlerChain chain) throws Exception {
        final String graphVertexId = getAttributeString(request, "graphVertexId");

        AppSession session = app.getAppSession(request);
        GraphVertex vertex = graphRepository.findVertex(session.getGraphSession(), graphVertexId);

        respondWithJson(response, vertex.toJson());
    }
}

