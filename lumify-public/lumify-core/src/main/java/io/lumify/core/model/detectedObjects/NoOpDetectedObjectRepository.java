package io.lumify.core.model.detectedObjects;

import com.altamiracorp.bigtable.model.FlushFlag;
import com.altamiracorp.bigtable.model.ModelSession;
import com.altamiracorp.bigtable.model.Row;
import com.altamiracorp.bigtable.model.user.ModelUserContext;
import org.securegraph.Authorizations;
import org.securegraph.Vertex;
import org.securegraph.Visibility;
import com.beust.jcommander.internal.Nullable;
import com.google.inject.Inject;
import org.json.JSONArray;
import org.json.JSONObject;

public class NoOpDetectedObjectRepository extends DetectedObjectRepository {
    @Inject
    public NoOpDetectedObjectRepository(@Nullable ModelSession modelSession) {
        super(modelSession);
    }

    @Override
    public DetectedObjectModel fromRow(Row row) {
        throw new RuntimeException("not supported");
    }

    @Override
    public Row toRow(DetectedObjectModel obj) {
        throw new RuntimeException("not supported");
    }

    @Override
    public String getTableName() {
        throw new RuntimeException("not supported");
    }

    @Override
    public DetectedObjectModel saveDetectedObject(Object artifactVertexId, Object edgeId, Object graphVertexId, String concept, double x1, double y1, double x2, double y2, boolean resolved, String process, Visibility visibility, ModelUserContext modelUserContext) {
        throw new RuntimeException("not supported");
    }

    @Override
    public JSONArray toJSON(Vertex artifactVertex, ModelUserContext modelUserContext, Authorizations authorizations, String workspaceId) {
        return new JSONArray();
    }

    @Override
    public JSONObject toJSON(DetectedObjectModel detectedObjectModel, Authorizations authorizations) {
        return new JSONObject();
    }

    @Override
    public void updateColumnVisibility(DetectedObjectModel detectedObjectModel, String originalEdgeVisibility, String visibilityString, FlushFlag flushFlag) {
        throw new RuntimeException("not supported");
    }
}
