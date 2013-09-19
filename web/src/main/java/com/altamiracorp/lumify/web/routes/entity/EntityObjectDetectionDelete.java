package com.altamiracorp.lumify.web.routes.entity;

import com.altamiracorp.lumify.AppSession;
import com.altamiracorp.lumify.model.Column;
import com.altamiracorp.lumify.model.ColumnFamily;
import com.altamiracorp.lumify.model.Repository;
import com.altamiracorp.lumify.model.Row;
import com.altamiracorp.lumify.model.graph.GraphRepository;
import com.altamiracorp.lumify.model.termMention.TermMention;
import com.altamiracorp.lumify.model.termMention.TermMentionRowKey;
import com.altamiracorp.lumify.ucd.artifact.Artifact;
import com.altamiracorp.lumify.ucd.artifact.ArtifactRepository;
import com.altamiracorp.lumify.ucd.artifact.ArtifactRowKey;
import com.altamiracorp.lumify.web.BaseRequestHandler;
import com.altamiracorp.web.HandlerChain;
import com.google.inject.Inject;
import org.json.JSONObject;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class EntityObjectDetectionDelete extends BaseRequestHandler {
    private final GraphRepository graphRepository;
    private final ArtifactRepository artifactRepository;

    @Inject
    public EntityObjectDetectionDelete(final ArtifactRepository artifactRepo,
                                       final GraphRepository graphRepo) {
        artifactRepository = artifactRepo;
        graphRepository = graphRepo;
    }

    @Override
    public void handle (HttpServletRequest request, HttpServletResponse response, HandlerChain chain) throws Exception {
        AppSession session = app.getAppSession(request);
        JSONObject jsonObject = new JSONObject(getRequiredParameter(request, "objectInfo"));

        // Delete from term mention
        TermMentionRowKey termMentionRowKey = new TermMentionRowKey(jsonObject.getString("_rowKey"));
        session.getModelSession().deleteRow(TermMention.TABLE_NAME, termMentionRowKey);

        // Delete from titan
        String graphVertexId = jsonObject.getString("graphVertexId");
        graphRepository.remove(session.getGraphSession(), graphVertexId);

        // Delete column from Artifact
        Artifact artifact = artifactRepository.findByRowKey(session.getModelSession(), termMentionRowKey.getArtifactRowKey());
        Row<ArtifactRowKey> rowKey = artifactRepository.toRow(artifact);
        String columnFamily = artifact.getArtifactDetectedObjects().getColumnFamilyName();
        String columnQualifier = jsonObject.getJSONObject("info").getString("_rowKey");
        for (Column column : rowKey.get(columnFamily).getColumns()){
            if (column.getName().equals(columnQualifier)){
                column.setDirty(true);
            }
        }
        session.getModelSession().deleteColumn(rowKey, Artifact.TABLE_NAME, columnFamily, columnQualifier);

        // Overwrite old ElasticSearch index
        Artifact newArtifact = artifactRepository.findByRowKey(session.getModelSession(), termMentionRowKey.getArtifactRowKey());
        session.getSearchProvider().add(newArtifact);
    }
}
