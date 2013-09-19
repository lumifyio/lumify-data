package com.altamiracorp.lumify.web.routes.entity;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONObject;

import com.altamiracorp.lumify.AppSession;
import com.altamiracorp.lumify.model.GraphSession;
import com.altamiracorp.lumify.model.Repository;
import com.altamiracorp.lumify.model.graph.GraphRepository;
import com.altamiracorp.lumify.model.graph.GraphVertex;
import com.altamiracorp.lumify.model.graph.InMemoryGraphVertex;
import com.altamiracorp.lumify.model.ontology.LabelName;
import com.altamiracorp.lumify.model.ontology.PropertyName;
import com.altamiracorp.lumify.model.ontology.VertexType;
import com.altamiracorp.lumify.model.termMention.TermMention;
import com.altamiracorp.lumify.model.termMention.TermMentionRowKey;
import com.altamiracorp.lumify.objectDetection.DetectedObject;
import com.altamiracorp.lumify.objectDetection.ObjectDetectionWorker;
import com.altamiracorp.lumify.ucd.artifact.Artifact;
import com.altamiracorp.lumify.ucd.artifact.ArtifactRepository;
import com.altamiracorp.lumify.web.BaseRequestHandler;
import com.altamiracorp.web.HandlerChain;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.inject.Inject;

public class EntityObjectDetectionCreate extends BaseRequestHandler {
    private final GraphRepository graphRepository;
    private final ArtifactRepository artifactRepository;
    private final Repository<TermMention> termMentionRepository;

    private final ExecutorService executorService = MoreExecutors.getExitingExecutorService(
            new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>()),
            0L, TimeUnit.MILLISECONDS);

    @Inject
    public EntityObjectDetectionCreate(final Repository<TermMention> termMentionRepo,
            final ArtifactRepository artifactRepo, final GraphRepository graphRepo) {
        termMentionRepository = termMentionRepo;
        artifactRepository = artifactRepo;
        graphRepository = graphRepo;
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, HandlerChain chain) throws Exception {
        EntityHelper objectDetectionHelper = new EntityHelper(termMentionRepository, graphRepository);

        // required parameters
        final String artifactRowKey = getRequiredParameter(request, "artifactKey");
        final String artifactId = getRequiredParameter(request, "artifactId");
        final String sign = getRequiredParameter(request, "sign");
        final String conceptId = getRequiredParameter(request, "conceptId");
        final String resolvedGraphVertexId = getOptionalParameter(request, "graphVertexId");
        final String x1 = getRequiredParameter(request, "x1");
        final String y1 = getRequiredParameter(request, "y1");
        final String x2 = getRequiredParameter(request, "x2");
        final String y2 = getRequiredParameter(request, "y2");
        String model = getOptionalParameter(request, "model");
        String detectedObjectRowKey = getOptionalParameter(request, "detectedObjectRowKey");
        final String boundingBox = "[x1: " + x1 + ", y1: " + y1 + ", x2: " + x2 + ", y2: " + y2 + "]";

        AppSession session = app.getAppSession(request);
        TermMentionRowKey termMentionRowKey = new TermMentionRowKey(artifactRowKey, 0, 0);

        GraphVertex conceptVertex = graphRepository.findVertex(session.getGraphSession(), conceptId);

        // create new graph vertex
        GraphVertex resolvedVertex = createGraphVertex(session.getGraphSession(), conceptVertex, resolvedGraphVertexId,
                sign, termMentionRowKey.toString(), boundingBox, artifactId);

        // create new term mention
        TermMention termMention = new TermMention(termMentionRowKey);
        objectDetectionHelper.updateTermMention(session, termMention, sign, conceptVertex, resolvedVertex);
        DetectedObject detectedObject = objectDetectionHelper.createObjectTag(x1, x2, y1, y2, resolvedVertex, conceptVertex);

        // create a new detected object column
        Artifact artifact = artifactRepository.findByRowKey(session.getModelSession(), artifactRowKey);

        if (detectedObjectRowKey == null && model == null) {
            model = "manual";
            detectedObject.setModel(model);
            detectedObjectRowKey = artifact.getArtifactDetectedObjects().addDetectedObject
                    (detectedObject.getConcept(), model, x1, y1, x2, y2);
        } else {
            detectedObject.setModel(model);
        }

        detectedObject.setRowKey(detectedObjectRowKey);
        JSONObject obj = detectedObject.getJson();
        executorService.execute(new ObjectDetectionWorker(session, artifactRowKey, detectedObjectRowKey, obj));

        respondWithJson(response, obj);
    }

    private GraphVertex createGraphVertex(GraphSession graphSession, GraphVertex conceptVertex, String resolvedGraphVertexId,
                                          String sign, String termMentionRowKey, String boundingBox, String artifactId) {
        GraphVertex resolvedVertex;
        if (resolvedGraphVertexId != null) {
            resolvedVertex = graphRepository.findVertex(graphSession, resolvedGraphVertexId);
        } else {
            resolvedVertex = graphRepository.findVertexByTitleAndType(graphSession, sign, VertexType.ENTITY);
            if (resolvedVertex == null) {
                resolvedVertex = new InMemoryGraphVertex();
                resolvedVertex.setType(VertexType.ENTITY);
            }
            resolvedVertex.setProperty(PropertyName.ROW_KEY, termMentionRowKey);
        }

        resolvedVertex.setProperty(PropertyName.SUBTYPE, conceptVertex.getId());
        resolvedVertex.setProperty(PropertyName.TITLE, sign);

        graphRepository.saveVertex(graphSession, resolvedVertex);

        graphRepository.saveRelationship(graphSession, artifactId, resolvedVertex.getId(), LabelName.CONTAINS_IMAGE_OF);
        graphRepository.setPropertyEdge(graphSession, artifactId, resolvedVertex.getId(), LabelName.CONTAINS_IMAGE_OF.toString()
                , PropertyName.BOUNDING_BOX.toString(), boundingBox);
        return resolvedVertex;
    }
}
