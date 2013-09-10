package com.altamiracorp.lumify.web.routes.vertex;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.altamiracorp.lumify.AppSession;
import com.altamiracorp.lumify.model.graph.GraphRepository;
import com.altamiracorp.lumify.web.BaseRequestHandler;
import com.altamiracorp.web.HandlerChain;

public class VertexRelationshipRemoval extends BaseRequestHandler {
    private GraphRepository graphRepository = new GraphRepository();

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, HandlerChain chain) throws Exception {
        final String source = getRequiredParameter(request, "sourceId");
        final String target = getRequiredParameter(request, "targetId");
        final String label = getRequiredParameter(request, "label");

        AppSession session = app.getAppSession(request);
        graphRepository.removeRelationship(session.getGraphSession(), source, target, label);
    }
}
