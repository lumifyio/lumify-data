package com.altamiracorp.lumify.web.routes.entity;

import com.altamiracorp.lumify.AppSession;
import com.altamiracorp.lumify.model.Repository;
import com.altamiracorp.lumify.model.Value;
import com.altamiracorp.lumify.model.graph.GraphRepository;
import com.altamiracorp.lumify.model.graph.GraphVertex;
import com.altamiracorp.lumify.model.ontology.PropertyName;
import com.altamiracorp.lumify.model.termMention.TermMention;
import com.altamiracorp.lumify.model.termMention.TermMentionRowKey;
import com.altamiracorp.lumify.objectDetection.DetectedObject;
import com.altamiracorp.lumify.objectDetection.ObjectDetectionWorker;
import com.altamiracorp.lumify.ucd.artifact.ArtifactDetectedObjects;
import com.altamiracorp.lumify.ucd.artifact.ArtifactRepository;
import com.altamiracorp.lumify.web.BaseRequestHandler;
import com.altamiracorp.web.HandlerChain;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.inject.Inject;
import org.json.JSONObject;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class EntityObjectDetectionUpdate extends BaseRequestHandler {
    private final GraphRepository graphRepository;
    private final ArtifactRepository artifactRepository;
    private final Repository<TermMention> termMentionRepository;

    private final ExecutorService executorService = MoreExecutors.getExitingExecutorService(
            new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>()),
            0L, TimeUnit.MILLISECONDS);

    @Inject
    public EntityObjectDetectionUpdate (final Repository<TermMention> termMentionRepo,
                            final ArtifactRepository artifactRepo,
                            final GraphRepository graphRepo) {
        termMentionRepository = termMentionRepo;
        artifactRepository = artifactRepo;
        graphRepository = graphRepo;
    }

    @Override
    public void handle (HttpServletRequest request, HttpServletResponse response, HandlerChain chain) throws Exception {
        EntityHelper objectDetectionHelper = new EntityHelper(termMentionRepository, graphRepository);

        final String artifactRowKey = getRequiredParameter(request, "artifactKey");
        final String sign = getRequiredParameter(request, "sign");
        final String conceptId = getRequiredParameter(request, "conceptId");
        final String resolvedGraphVertexId = getRequiredParameter(request, "graphVertexId");
        final String x1 = getRequiredParameter(request, "x1");
        final String y1 = getRequiredParameter(request, "y1");
        final String x2 = getRequiredParameter(request, "x2");
        final String y2 = getRequiredParameter(request, "y2");
        String detectedObjectRowKey = getRequiredParameter(request, "detectedObjectRowKey");

        AppSession session = app.getAppSession(request);
        GraphVertex conceptVertex = graphRepository.findVertex(session.getGraphSession(), conceptId);
        GraphVertex resolvedVertex = graphRepository.findVertex(session.getGraphSession(), resolvedGraphVertexId);

        // update graph vertex
        resolvedVertex.setProperty(PropertyName.SUBTYPE, conceptVertex.getId());
        resolvedVertex.setProperty(PropertyName.TITLE, sign);
        graphRepository.saveVertex(session.getGraphSession(), resolvedVertex);

        // update the term mention
        TermMentionRowKey termMentionRowKey = new TermMentionRowKey(artifactRowKey, 0, 0);
        TermMention termMention = termMentionRepository.findByRowKey(session.getModelSession(), termMentionRowKey.toString());
        objectDetectionHelper.updateTermMention(session, termMention, sign, conceptVertex, resolvedVertex);

        // update the detected object column
        ArtifactDetectedObjects artifactDetectedObjects = artifactRepository.findByRowKey(session.getModelSession(), artifactRowKey).getArtifactDetectedObjects();

        DetectedObject detectedObject = objectDetectionHelper.createObjectTag(x1, x2, y1, y2, resolvedVertex, conceptVertex);
        detectedObject.setRowKey(detectedObjectRowKey);

        JSONObject value = Value.toJson(artifactDetectedObjects.get(detectedObjectRowKey));
        JSONObject info = value.getJSONObject("info");
        detectedObject.setModel(info.get("model").toString());

        JSONObject obj = detectedObject.getJson();
        executorService.execute(new ObjectDetectionWorker(session, artifactRowKey, detectedObjectRowKey, obj));

        respondWithJson(response, obj);
    }
}