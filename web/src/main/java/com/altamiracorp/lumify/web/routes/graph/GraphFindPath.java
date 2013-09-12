package com.altamiracorp.lumify.web.routes.graph;

import com.altamiracorp.lumify.AppSession;
import com.altamiracorp.lumify.model.graph.GraphRepository;
import com.altamiracorp.lumify.model.graph.GraphVertex;
import com.altamiracorp.lumify.web.BaseRequestHandler;
import com.altamiracorp.web.HandlerChain;
import org.json.JSONObject;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

public class GraphFindPath extends BaseRequestHandler {
    private GraphRepository graphRepository = new GraphRepository();


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

