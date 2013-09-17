package com.altamiracorp.lumify.web.routes.relationship;

import com.altamiracorp.lumify.AppSession;
import com.altamiracorp.lumify.model.graph.GraphRelationship;
import com.altamiracorp.lumify.model.graph.GraphRepository;
import com.altamiracorp.lumify.web.BaseRequestHandler;
import com.altamiracorp.web.HandlerChain;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class RelationshipCreate extends BaseRequestHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(RelationshipCreate.class);
    private GraphRepository graphRepository = new GraphRepository();

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, HandlerChain chain) throws Exception {
        // validate parameters
        final String sourceGraphVertexId = getRequiredParameter(request, "sourceGraphVertexId");
        final String destGraphVertexId = getRequiredParameter(request, "destGraphVertexId");
        final String predicateLabel = getRequiredParameter(request, "predicateLabel");

        AppSession session = app.getAppSession(request);
        GraphRelationship relationship = graphRepository.saveRelationship(session.getGraphSession(), sourceGraphVertexId, destGraphVertexId, predicateLabel);

        LOGGER.info("Statement created:\n" + relationship.toJson().toString(2));

        respondWithJson(response, relationship.toJson());
    }
}
