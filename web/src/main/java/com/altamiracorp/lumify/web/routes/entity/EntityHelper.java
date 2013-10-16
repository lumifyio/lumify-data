package com.altamiracorp.lumify.web.routes.entity;

import com.altamiracorp.lumify.core.ingest.ArtifactDetectedObject;
import com.altamiracorp.lumify.core.model.graph.GraphVertex;
import com.altamiracorp.lumify.core.model.ontology.PropertyName;
import com.altamiracorp.lumify.core.user.User;
import com.altamiracorp.lumify.model.graph.GraphRepository;
import com.altamiracorp.lumify.model.termMention.TermMention;
import com.altamiracorp.lumify.model.termMention.TermMentionRepository;
import com.altamiracorp.lumify.model.workQueue.WorkQueueRepository;
import com.google.inject.Inject;

public class EntityHelper {
    private final GraphRepository graphRepository;
    private final TermMentionRepository termMentionRepository;
    private final WorkQueueRepository workQueueRepository;

    @Inject
    public EntityHelper(final TermMentionRepository termMentionRepository,
                        final GraphRepository graphRepository, WorkQueueRepository workQueueRepository) {
        this.termMentionRepository = termMentionRepository;
        this.graphRepository = graphRepository;
        this.workQueueRepository = workQueueRepository;
    }

    public void updateTermMention(TermMention termMention, String sign, GraphVertex conceptVertex, GraphVertex resolvedVertex, User user) {
        termMention.getMetadata()
                .setSign(sign)
                .setOntologyClassUri((String) conceptVertex.getProperty(PropertyName.DISPLAY_NAME))
                .setConceptGraphVertexId(conceptVertex.getId())
                .setGraphVertexId(resolvedVertex.getId());
        termMentionRepository.save(termMention, user);
    }

    public void updateGraphVertex(GraphVertex vertex, String subType, String title, User user) {
        vertex.setProperty(PropertyName.SUBTYPE, subType);
        vertex.setProperty(PropertyName.TITLE, title);

        graphRepository.saveVertex(vertex, user);
    }

    public ArtifactDetectedObject createObjectTag(String x1, String x2, String y1, String y2, GraphVertex resolvedVertex, GraphVertex conceptVertex) {
        ArtifactDetectedObject detectedObject = new ArtifactDetectedObject(x1, y1, x2, y2);
        detectedObject.setGraphVertexId(resolvedVertex.getId().toString());

        if (conceptVertex.getProperty("ontologyTitle").toString().equals("person")) {
            detectedObject.setConcept("face");
        } else {
            detectedObject.setConcept(conceptVertex.getProperty("ontologyTitle").toString());
        }
        detectedObject.setResolvedVertex(resolvedVertex);

        return detectedObject;
    }

    public void scheduleHighlight(String artifactGraphVertexId, User user) {
        this.workQueueRepository.pushArtifactHighlight(artifactGraphVertexId);
    }
}
