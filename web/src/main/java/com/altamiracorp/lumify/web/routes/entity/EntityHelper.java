package com.altamiracorp.lumify.web.routes.entity;

import com.altamiracorp.lumify.core.user.User;
import com.altamiracorp.lumify.model.Repository;
import com.altamiracorp.lumify.model.graph.GraphRepository;
import com.altamiracorp.lumify.model.graph.GraphVertex;
import com.altamiracorp.lumify.model.ontology.LabelName;
import com.altamiracorp.lumify.model.ontology.PropertyName;
import com.altamiracorp.lumify.model.termMention.TermMention;
import com.altamiracorp.lumify.objectDetection.DetectedObject;
import com.google.common.util.concurrent.MoreExecutors;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class EntityHelper {
    private final GraphRepository graphRepository;
    private final Repository<TermMention> termMentionRepository;

    private final ExecutorService executorService = MoreExecutors.getExitingExecutorService(
            new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>()),
            0L, TimeUnit.MILLISECONDS);

    public EntityHelper(final Repository<TermMention> termMentionRepo,
                        final GraphRepository graphRepo) {
        termMentionRepository = termMentionRepo;
        graphRepository = graphRepo;
    }

    public void updateTermMention(TermMention termMention, String sign, GraphVertex conceptVertex, GraphVertex resolvedVertex, User user) {
        termMention.getMetadata()
                .setSign(sign)
                .setConcept((String) conceptVertex.getProperty(PropertyName.DISPLAY_NAME))
                .setConceptGraphVertexId(conceptVertex.getId())
                .setGraphVertexId(resolvedVertex.getId());
        termMentionRepository.save(termMention, user);
    }

    public void updateGraphVertex(GraphVertex vertex, String subType, String title, String artifactId, User user) {
        vertex.setProperty(PropertyName.SUBTYPE, subType);
        vertex.setProperty(PropertyName.TITLE, title);

        graphRepository.saveVertex(vertex, user);
        graphRepository.saveRelationship(artifactId, vertex.getId(), LabelName.HAS_ENTITY, user);
    }

    public DetectedObject createObjectTag(String x1, String x2, String y1, String y2, GraphVertex resolvedVertex, GraphVertex conceptVertex) {
        DetectedObject detectedObject = new DetectedObject(x1, y1, x2, y2);
        detectedObject.setGraphVertexId(resolvedVertex.getId().toString());

        if (conceptVertex.getProperty("ontologyTitle").toString().equals("person")) {
            detectedObject.setConcept("face");
        } else {
            detectedObject.setConcept(conceptVertex.getProperty("ontologyTitle").toString());
        }
        detectedObject.setResolvedVertex(resolvedVertex);

        return detectedObject;
    }

    public void executeService(Runnable helperClass) {
        executorService.execute(helperClass);
    }

}
