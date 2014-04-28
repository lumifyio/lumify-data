package io.lumify.core.model.audit;

import com.altamiracorp.bigtable.model.FlushFlag;
import com.altamiracorp.bigtable.model.ModelSession;
import com.altamiracorp.bigtable.model.Row;
import io.lumify.core.user.User;
import org.securegraph.Edge;
import org.securegraph.Vertex;
import org.securegraph.Visibility;
import org.securegraph.mutation.ElementMutation;
import com.beust.jcommander.internal.Nullable;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import java.util.List;
import java.util.Map;

@Singleton
public class NoOpAuditRepository extends AuditRepository {
    @Inject
    public NoOpAuditRepository(@Nullable final ModelSession modelSession) {
        super(modelSession);
    }

    @Override
    public Audit fromRow(Row row) {
        throw new RuntimeException("not supported");
    }

    @Override
    public Row toRow(Audit audit) {
        throw new RuntimeException("not supported");
    }

    @Override
    public String getTableName() {
        throw new RuntimeException("not supported");
    }

    @Override
    public Audit auditVertex(AuditAction auditAction, Object vertexId, String process, String comment, User user, FlushFlag flushFlag, Visibility visibility) {
        throw new RuntimeException("not supported");
    }

    @Override
    public Audit auditEntityProperty(AuditAction action, Object id, String propertyKey, String propertyName, Object oldValue,
                                     Object newValue, String process, String comment, Map<String, Object> metadata, User user, Visibility visibility) {
        throw new RuntimeException("not supported");
    }

    @Override
    public List<Audit> auditRelationship(AuditAction action, Vertex sourceVertex, Vertex destVertex, Edge edge, String process, String comment, User user, Visibility visibility) {
        throw new RuntimeException("not supported");
    }

    @Override
    public List<Audit> auditRelationshipProperty(AuditAction action, String sourceId, String destId, String propertyKey,
                                                 String propertyName, Object oldValue, Object newValue, Edge edge, String process, String comment, User user, Visibility visibility) {
        throw new RuntimeException("not supported");
    }

    @Override
    public void auditVertexElementMutation(AuditAction action, ElementMutation<Vertex> vertexElementMutation, Vertex vertex, String process, User user, Visibility visibility) {
        throw new RuntimeException("not supported");
    }

    @Override
    public void auditEdgeElementMutation(AuditAction action, ElementMutation<Edge> edgeElementMutation, Edge edge, Vertex sourceVertex, Vertex destVertex, String process, User user, Visibility visibility) {
        throw new RuntimeException("not supported");
    }

    @Override
    public void updateColumnVisibility(Audit audit, Visibility originalEdgeVisibility, String visibilityString, FlushFlag flushFlag) {
        throw new RuntimeException("not supported");
    }
}
