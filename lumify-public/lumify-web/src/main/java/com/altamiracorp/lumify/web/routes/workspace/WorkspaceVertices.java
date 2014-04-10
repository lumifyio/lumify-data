package com.altamiracorp.lumify.web.routes.workspace;

import com.altamiracorp.lumify.core.config.Configuration;
import com.altamiracorp.lumify.core.model.user.UserRepository;
import com.altamiracorp.lumify.core.model.workspace.Workspace;
import com.altamiracorp.lumify.core.model.workspace.WorkspaceEntity;
import com.altamiracorp.lumify.core.model.workspace.WorkspaceRepository;
import com.altamiracorp.lumify.core.user.User;
import com.altamiracorp.lumify.core.util.GraphUtil;
import com.altamiracorp.lumify.core.util.LumifyLogger;
import com.altamiracorp.lumify.core.util.LumifyLoggerFactory;
import com.altamiracorp.lumify.web.BaseRequestHandler;
import com.altamiracorp.miniweb.HandlerChain;
import com.altamiracorp.securegraph.Authorizations;
import com.altamiracorp.securegraph.Graph;
import com.altamiracorp.securegraph.Vertex;
import com.altamiracorp.securegraph.util.LookAheadIterable;
import com.google.inject.Inject;
import org.json.JSONArray;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Iterator;
import java.util.List;

public class WorkspaceVertices extends BaseRequestHandler {
    private static final LumifyLogger LOGGER = LumifyLoggerFactory.getLogger(WorkspaceVertices.class);
    private final Graph graph;
    private final WorkspaceRepository workspaceRepository;

    @Inject
    public WorkspaceVertices(
            final Graph graph,
            final UserRepository userRepository,
            final Configuration configuration,
            final WorkspaceRepository workspaceRepository) {
        super(userRepository, configuration);
        this.graph = graph;
        this.workspaceRepository = workspaceRepository;
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, HandlerChain chain) throws Exception {
        User user = getUser(request);
        Authorizations authorizations = getAuthorizations(request, user);
        String workspaceId = getActiveWorkspaceId(request);

        Workspace workspace = workspaceRepository.findById(workspaceId, user);
        final List<WorkspaceEntity> workspaceEntities = workspaceRepository.findEntities(workspace, user);
        Iterable<Object> vertexIds = getVisibleWorkspaceEntityIds(workspaceEntities);
        Iterable<Vertex> graphVertices = graph.getVertices(vertexIds, authorizations);
        JSONArray results = GraphUtil.toJson(graphVertices, workspaceId);
        respondWithJson(response, results);
    }

    private LookAheadIterable<WorkspaceEntity, Object> getVisibleWorkspaceEntityIds(final List<WorkspaceEntity> workspaceEntities) {
        return new LookAheadIterable<WorkspaceEntity, Object>() {
            @Override
            protected boolean isIncluded(WorkspaceEntity workspaceEntity, Object entityVertexId) {
                return workspaceEntity.isVisible();
            }

            @Override
            protected Object convert(WorkspaceEntity workspaceEntity) {
                return workspaceEntity.getEntityVertexId();
            }

            @Override
            protected Iterator<WorkspaceEntity> createIterator() {
                return workspaceEntities.iterator();
            }
        };
    }
}
