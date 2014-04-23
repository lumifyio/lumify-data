package io.lumify.web.routes.vertex;

import io.lumify.core.config.Configuration;
import io.lumify.core.model.user.UserRepository;
import io.lumify.core.user.User;
import io.lumify.core.util.JsonSerializer;
import io.lumify.web.BaseRequestHandler;
import com.altamiracorp.miniweb.HandlerChain;
import org.securegraph.Authorizations;
import org.securegraph.Graph;
import org.securegraph.Vertex;
import org.securegraph.util.ConvertingIterable;
import com.google.inject.Inject;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static io.lumify.core.util.CollectionUtil.toIterable;

public class VertexMultiple extends BaseRequestHandler {
    private final Graph graph;

    @Inject
    public VertexMultiple(
            final Graph graph,
            final UserRepository userRepository,
            final Configuration configuration) {
        super(userRepository, configuration);
        this.graph = graph;
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, HandlerChain chain) throws Exception {
        String[] vertexStringIds = request.getParameterValues("vertexIds[]");
        User user = getUser(request);
        Authorizations authorizations = getAuthorizations(request, user);
        String workspaceId = getActiveWorkspaceId(request);

        Iterable<Object> vertexIds = new ConvertingIterable<String, Object>(toIterable(vertexStringIds)) {
            @Override
            protected Object convert(String s) {
                return s;
            }
        };

        Iterable<Vertex> graphVertices = graph.getVertices(vertexIds, authorizations);
        JSONObject results = new JSONObject();
        JSONArray vertices = new JSONArray();
        results.put("vertices", vertices);
        for (Vertex v : graphVertices) {
            vertices.put(JsonSerializer.toJson(v, workspaceId));
        }

        respondWithJson(response, results);
    }
}
