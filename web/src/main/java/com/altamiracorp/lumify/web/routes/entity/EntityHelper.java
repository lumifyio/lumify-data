package com.altamiracorp.lumify.web.routes.entity;

import com.altamiracorp.lumify.AppSession;
import com.altamiracorp.lumify.model.Repository;
import com.altamiracorp.lumify.model.graph.GraphRepository;
import com.altamiracorp.lumify.model.graph.GraphVertex;
import com.altamiracorp.lumify.model.ontology.PropertyName;
import com.altamiracorp.lumify.model.termMention.TermMention;
import com.altamiracorp.lumify.objectDetection.DetectedObject;
import com.altamiracorp.lumify.ucd.artifact.ArtifactRepository;

public class EntityHelper {
    private final GraphRepository graphRepository;
    private final ArtifactRepository artifactRepository;
    private final Repository<TermMention> termMentionRepository;

    public EntityHelper(final Repository<TermMention> termMentionRepo,
                        final ArtifactRepository artifactRepo,
                        final GraphRepository graphRepo) {
        termMentionRepository = termMentionRepo;
        artifactRepository = artifactRepo;
        graphRepository = graphRepo;
    }

    public void updateTermMention(AppSession session, TermMention termMention, String sign, GraphVertex conceptVertex, GraphVertex resolvedVertex) {
        termMention.getMetadata()
                .setSign(sign)
                .setConcept((String) conceptVertex.getProperty(PropertyName.DISPLAY_NAME))
                .setConceptGraphVertexId(conceptVertex.getId())
                        .setGraphVertexId(resolvedVertex.getId());
        termMentionRepository.save(session.getModelSession(), termMention);
    }

    public DetectedObject createObjectTag (String x1, String x2, String y1, String y2, GraphVertex resolvedVertex, GraphVertex conceptVertex){
        DetectedObject detectedObject = new DetectedObject(x1, y1, x2, y2);
        detectedObject.setGraphVertexId(resolvedVertex.getId().toString());

        if (conceptVertex.getProperty("ontologyTitle").toString().equals("person")){
            detectedObject.setConcept("face");
        } else {
            detectedObject.setConcept(conceptVertex.getProperty("ontologyTitle").toString());
        }
        detectedObject.setResolvedVertex(resolvedVertex);

        return detectedObject;
    }

}
