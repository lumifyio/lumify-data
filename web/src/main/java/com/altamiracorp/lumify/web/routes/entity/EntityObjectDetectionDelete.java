package com.altamiracorp.lumify.web.routes.entity;

import com.altamiracorp.lumify.AppSession;
import com.altamiracorp.lumify.model.Column;
import com.altamiracorp.lumify.model.ColumnFamily;
import com.altamiracorp.lumify.model.Repository;
import com.altamiracorp.lumify.model.Row;
import com.altamiracorp.lumify.model.graph.GraphRelationship;
import com.altamiracorp.lumify.model.graph.GraphRepository;
import com.altamiracorp.lumify.model.graph.GraphVertex;
import com.altamiracorp.lumify.model.ontology.LabelName;
import com.altamiracorp.lumify.model.ontology.PropertyName;
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
import java.util.Map;

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

        // Delete just the relationship if vertex has more than one relationship otherwise delete vertex
        String graphVertexId = jsonObject.getString("graphVertexId");
        JSONObject obj = graphRepository.findVertex(session.getGraphSession(), graphVertexId).toJson();
        Map<GraphRelationship, GraphVertex> relationships = graphRepository.getRelationships(session.getGraphSession(), graphVertexId);
        if (relationships.size() > 0) {
            GraphVertex artifactVertex = graphRepository.findVertexByRowKey(session.getGraphSession(), termMentionRowKey.getArtifactRowKey().toString());
            String edgeId = artifactVertex.getId() + ">" + graphVertexId + "|" + LabelName.CONTAINS_IMAGE_OF.toString();
            obj.put("edgeId", edgeId);
            graphRepository.removeRelationship(session.getGraphSession(), artifactVertex.getId(), graphVertexId, LabelName.CONTAINS_IMAGE_OF.toString());
        } else {
            graphRepository.remove(session.getGraphSession(), graphVertexId);
            obj.put("remove", true);
        }

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

        respondWithJson(response, obj);
    }
}
