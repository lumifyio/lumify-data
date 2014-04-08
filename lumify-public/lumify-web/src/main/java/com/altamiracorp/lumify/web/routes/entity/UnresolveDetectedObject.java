package com.altamiracorp.lumify.web.routes.entity;

import com.altamiracorp.bigtable.model.user.ModelUserContext;
import com.altamiracorp.lumify.core.config.Configuration;
import com.altamiracorp.lumify.core.model.detectedObjects.DetectedObjectModel;
import com.altamiracorp.lumify.core.model.detectedObjects.DetectedObjectRepository;
import com.altamiracorp.lumify.core.model.detectedObjects.DetectedObjectRowKey;
import com.altamiracorp.lumify.core.model.user.UserRepository;
import com.altamiracorp.lumify.core.model.workspace.diff.SandboxStatus;
import com.altamiracorp.lumify.core.security.LumifyVisibility;
import com.altamiracorp.lumify.core.security.LumifyVisibilityProperties;
import com.altamiracorp.lumify.core.security.VisibilityTranslator;
import com.altamiracorp.lumify.core.user.User;
import com.altamiracorp.lumify.core.util.GraphUtil;
import com.altamiracorp.lumify.core.util.LumifyLogger;
import com.altamiracorp.lumify.core.util.LumifyLoggerFactory;
import com.altamiracorp.lumify.web.BaseRequestHandler;
import com.altamiracorp.lumify.web.routes.workspace.WorkspaceHelper;
import com.altamiracorp.miniweb.HandlerChain;
import com.altamiracorp.securegraph.Authorizations;
import com.altamiracorp.securegraph.Edge;
import com.altamiracorp.securegraph.Graph;
import com.altamiracorp.securegraph.Vertex;
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
            final WorkspaceHelper workspaceHelper) {
        super(userRepository, configuration);
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
            visibilityJson = GraphUtil.updateVisibilityJsonRemoveFromWorkspace(edge.getPropertyValue(LumifyVisibilityProperties.VISIBILITY_JSON_PROPERTY.toString()).toString(), workspaceId);
        } else {
            visibilityJson = GraphUtil.updateVisibilityJsonRemoveFromWorkspace(resolvedVertex.getPropertyValue(LumifyVisibilityProperties.VISIBILITY_JSON_PROPERTY.toString()).toString(), workspaceId);
        }
        LumifyVisibility lumifyVisibility = visibilityTranslator.toVisibility(visibilityJson);

        JSONObject result = workspaceHelper.unresolveDetectedObject(resolvedVertex, detectedObjectModel.getMetadata().getEdgeId(), detectedObjectModel, analyzedDetectedModel, lumifyVisibility,
                workspaceId, modelUserContext, user, authorizations);

        respondWithJson(response, result);
    }
}
