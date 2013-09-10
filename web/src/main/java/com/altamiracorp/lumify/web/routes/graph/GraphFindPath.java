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

public class GraphFindPath extends BaseRequestHandler {
    private GraphRepository graphRepository = new GraphRepository();


    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, HandlerChain chain) throws Exception {
        AppSession session = app.getAppSession(request);

        int depth;
        String depthStr = request.getParameter("depth");
        if (depthStr == null) {
            depth = 5;
        } else {
            depth = Integer.parseInt(depthStr);
        }

        String sourceGraphVertexId = request.getParameter("sourceGraphVertexId");
        if (sourceGraphVertexId == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "sourceGraphVertexId is required");
            return;
        }

        String destGraphVertexId = request.getParameter("destGraphVertexId");
        if (destGraphVertexId == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "destGraphVertexId is required");
            return;
        }

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

        List<List<GraphVertex>> vertices = graphRepository.findPath(session.getGraphSession(), sourceVertex, destVertex, depth);

        JSONObject resultsJson = new JSONObject();
        resultsJson.put("paths", GraphVertex.toJsonPath(vertices));

        respondWithJson(response, resultsJson);
    }
}

