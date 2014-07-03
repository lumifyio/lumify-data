package io.lumify.web.routes.relationship;

import io.lumify.core.config.Configuration;
import io.lumify.core.model.ontology.OntologyRepository;
import io.lumify.core.model.user.UserRepository;
import io.lumify.core.model.workspace.WorkspaceRepository;
import io.lumify.core.user.User;
import io.lumify.core.util.JsonSerializer;
import io.lumify.web.BaseRequestHandler;
import com.altamiracorp.miniweb.HandlerChain;
import org.securegraph.*;
import com.google.inject.Inject;
import org.json.JSONObject;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class RelationshipProperties extends BaseRequestHandler {
    private final Graph graph;
    private final OntologyRepository ontologyRepository;

    @Inject
    public RelationshipProperties(
            final OntologyRepository ontologyRepository,
            final Graph graph,
            final UserRepository userRepository,
            final WorkspaceRepository workspaceRepository,
            final Configuration configuration) {
        super(userRepository, workspaceRepository, configuration);
        this.ontologyRepository = ontologyRepository;
        this.graph = graph;
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, HandlerChain chain) throws Exception {
        final String graphEdgeId = getAttributeString(request, "graphEdgeId");

        User user = getUser(request);
        Authorizations authorizations = getAuthorizations(request, user);
        String workspaceId = getActiveWorkspaceId(request);

        Edge edge = graph.getEdge(graphEdgeId, authorizations);
        Vertex sourceVertex = edge.getVertex(Direction.OUT, authorizations);
        Vertex targetVertex = edge.getVertex(Direction.IN, authorizations);

        JSONObject results = JsonSerializer.toJson(edge, workspaceId);
        results.put("source", JsonSerializer.toJson(sourceVertex, workspaceId));
        results.put("target", JsonSerializer.toJson(targetVertex, workspaceId));

        respondWithJson(response, results);
    }
}
