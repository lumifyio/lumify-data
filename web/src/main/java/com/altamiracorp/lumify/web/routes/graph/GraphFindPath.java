package com.altamiracorp.lumify.web.routes.graph;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONObject;

import com.altamiracorp.lumify.AppSession;
import com.altamiracorp.lumify.model.graph.GraphRepository;
import com.altamiracorp.lumify.model.graph.GraphVertex;
import com.altamiracorp.lumify.web.BaseRequestHandler;
import com.altamiracorp.web.HandlerChain;
import com.google.inject.Inject;

public class GraphFindPath extends BaseRequestHandler {
    private final GraphRepository graphRepository;

    @Inject
    public GraphFindPath(final GraphRepository repo) {
        graphRepository = repo;
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, HandlerChain chain) throws Exception {
        AppSession session = app.getAppSession(request);

        int depth;
        String depthStr = getOptionalParameter(request, "depth");
        if (depthStr == null) {
            depth = 5;
        } else {
            depth = Integer.parseInt(depthStr);
        }

        final String sourceGraphVertexId = getRequiredParameter(request, "sourceGraphVertexId");
        final String destGraphVertexId = getRequiredParameter(request, "destGraphVertexId");
        final Integer hops = Integer.parseInt(getRequiredParameter(request,"hops"));

        GraphVertex sourceVertex = graphRepository.findVertex(session.getGraphSession(), sourceGraphVertexId);
        if (sourceVertex == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Source vertex not found");
            return;
        }

        GraphVertex destVertex = graphRepository.findVertex(session.getGraphSession(), destGraphVertexId);
        if (destVertex == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Destination vertex not found");
            return;
        }

        List<List<GraphVertex>> vertices = graphRepository.findPath(session.getGraphSession(), sourceVertex, destVertex, depth, hops);

        JSONObject resultsJson = new JSONObject();
        resultsJson.put("paths", GraphVertex.toJsonPath(vertices));

        respondWithJson(response, resultsJson);
    }
}

