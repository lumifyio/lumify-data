package io.lumify.web.routes.vertex;

import com.altamiracorp.bigtable.model.user.ModelUserContext;
import io.lumify.core.config.Configuration;
import io.lumify.core.model.detectedObjects.DetectedObjectRepository;
import io.lumify.core.model.user.UserRepository;
import io.lumify.core.user.User;
import io.lumify.core.util.JsonSerializer;
import io.lumify.web.BaseRequestHandler;
import com.altamiracorp.miniweb.HandlerChain;
import com.altamiracorp.securegraph.Authorizations;
import com.altamiracorp.securegraph.Graph;
import com.altamiracorp.securegraph.Vertex;
import com.google.inject.Inject;
import org.json.JSONObject;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class VertexProperties extends BaseRequestHandler {
    private final Graph graph;
    private final DetectedObjectRepository detectedObjectRepository;
    private final UserRepository userRepository;

    @Inject
    public VertexProperties(
            final Graph graph,
            final UserRepository userRepository,
            final Configuration configuration,
            final DetectedObjectRepository detectedObjectRepository) {
        super(userRepository, configuration);
        this.graph = graph;
        this.detectedObjectRepository = detectedObjectRepository;
        this.userRepository = userRepository;
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, HandlerChain chain) throws Exception {
        final String graphVertexId = getAttributeString(request, "graphVertexId");
        User user = getUser(request);
        Authorizations authorizations = getAuthorizations(request, user);
        String workspaceId = getActiveWorkspaceId(request);
        ModelUserContext modelUserContext = userRepository.getModelUserContext(authorizations, workspaceId);

        Vertex vertex = graph.getVertex(graphVertexId, authorizations);
        if (vertex == null) {
            respondWithNotFound(response);
            return;
        }
        JSONObject json = JsonSerializer.toJson(vertex, workspaceId);

        json.put("detectedObjects", detectedObjectRepository.toJSON(vertex, modelUserContext, authorizations, workspaceId));

        respondWithJson(response, json);
    }
}
