package io.lumify.web.routes.entity;

import com.altamiracorp.bigtable.model.user.ModelUserContext;
import io.lumify.core.config.Configuration;
import io.lumify.core.model.detectedObjects.DetectedObjectModel;
import io.lumify.core.model.detectedObjects.DetectedObjectRepository;
import io.lumify.core.model.detectedObjects.DetectedObjectRowKey;
import io.lumify.core.model.user.UserRepository;
import io.lumify.core.model.workspace.WorkspaceRepository;
import io.lumify.core.model.workspace.diff.SandboxStatus;
import io.lumify.core.security.LumifyVisibility;
import io.lumify.core.security.LumifyVisibilityProperties;
import io.lumify.core.security.VisibilityTranslator;
import io.lumify.core.user.User;
import io.lumify.core.util.GraphUtil;
import io.lumify.core.util.LumifyLogger;
import io.lumify.core.util.LumifyLoggerFactory;
import io.lumify.web.BaseRequestHandler;
import io.lumify.web.routes.workspace.WorkspaceHelper;
import com.altamiracorp.miniweb.HandlerChain;
import org.securegraph.Authorizations;
import org.securegraph.Edge;
import org.securegraph.Graph;
import org.securegraph.Vertex;
import com.google.inject.Inject;
import org.json.JSONObject;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class UnresolveDetectedObject extends BaseRequestHandler {
    private static final LumifyLogger LOGGER = LumifyLoggerFactory.getLogger(UnresolveDetectedObject.class);
    private final Graph graph;
    private final DetectedObjectRepository detectedObjectRepository;
    private final VisibilityTranslator visibilityTranslator;
    private final UserRepository userRepository;
    private final WorkspaceHelper workspaceHelper;

    @Inject
    public UnresolveDetectedObject(
            final Graph graph,
            final UserRepository userRepository,
            final DetectedObjectRepository detectedObjectRepository,
            final VisibilityTranslator visibilityTranslator,
            final Configuration configuration,
            final WorkspaceRepository workspaceRepository,
            final WorkspaceHelper workspaceHelper) {
        super(userRepository, workspaceRepository, configuration);
        this.graph = graph;
        this.detectedObjectRepository = detectedObjectRepository;
        this.visibilityTranslator = visibilityTranslator;
        this.userRepository = userRepository;
        this.workspaceHelper = workspaceHelper;
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, HandlerChain chain) throws Exception {
        final String rowKey = getRequiredParameter(request, "rowKey");
        final String visibilitySource = getRequiredParameter(request, "visibilitySource");
        String workspaceId = getActiveWorkspaceId(request);
        User user = getUser(request);
        Authorizations authorizations = getAuthorizations(request, user);
        ModelUserContext modelUserContext = userRepository.getModelUserContext(authorizations, workspaceId);

        DetectedObjectModel detectedObjectModel = detectedObjectRepository.findByRowKey(rowKey, modelUserContext);
        DetectedObjectRowKey detectedObjectRowKey = new DetectedObjectRowKey(rowKey);
        DetectedObjectRowKey analyzedDetectedObjectRK = new DetectedObjectRowKey
                (detectedObjectRowKey.getArtifactId(), detectedObjectModel.getMetadata().getX1(), detectedObjectModel.getMetadata().getY1(),
                        detectedObjectModel.getMetadata().getX2(), detectedObjectModel.getMetadata().getY2());
        DetectedObjectModel analyzedDetectedModel = detectedObjectRepository.findByRowKey(analyzedDetectedObjectRK.toString(), modelUserContext);
        Object resolvedId = detectedObjectModel.getMetadata().getResolvedId();

        Vertex resolvedVertex = graph.getVertex(resolvedId, authorizations);
        Edge edge = graph.getEdge(detectedObjectRowKey.getEdgeId(), authorizations);

        SandboxStatus vertexSandboxStatus = GraphUtil.getSandboxStatus(resolvedVertex, workspaceId);
        SandboxStatus edgeSandboxStatus = GraphUtil.getSandboxStatus(edge, workspaceId);
        if (vertexSandboxStatus == SandboxStatus.PUBLIC && edgeSandboxStatus == SandboxStatus.PUBLIC) {
            LOGGER.warn("Can not unresolve a public entity");
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            chain.next(request, response);
            return;
        }

        JSONObject visibilityJson;
        if (vertexSandboxStatus == SandboxStatus.PUBLIC) {
            visibilityJson = LumifyVisibilityProperties.VISIBILITY_JSON_PROPERTY.getPropertyValue(edge);
            visibilityJson = GraphUtil.updateVisibilityJsonRemoveFromWorkspace(visibilityJson, workspaceId);
        } else {
            visibilityJson = LumifyVisibilityProperties.VISIBILITY_JSON_PROPERTY.getPropertyValue(resolvedVertex);
            visibilityJson = GraphUtil.updateVisibilityJsonRemoveFromWorkspace(visibilityJson, workspaceId);
        }
        LumifyVisibility lumifyVisibility = visibilityTranslator.toVisibility(visibilityJson);

        JSONObject result = workspaceHelper.unresolveDetectedObject(resolvedVertex, detectedObjectModel.getMetadata().getEdgeId(), detectedObjectModel, analyzedDetectedModel, lumifyVisibility,
                workspaceId, modelUserContext, user, authorizations);

        respondWithJson(response, result);
    }
}
