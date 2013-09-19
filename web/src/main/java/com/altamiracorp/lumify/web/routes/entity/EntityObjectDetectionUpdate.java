package com.altamiracorp.lumify.web.routes.entity;

import com.altamiracorp.lumify.core.user.User;
import com.altamiracorp.lumify.model.Value;
import com.altamiracorp.lumify.model.graph.GraphRepository;
import com.altamiracorp.lumify.model.graph.GraphVertex;
import com.altamiracorp.lumify.model.ontology.PropertyName;
import com.altamiracorp.lumify.model.termMention.TermMention;
import com.altamiracorp.lumify.model.termMention.TermMentionRepository;
import com.altamiracorp.lumify.model.termMention.TermMentionRowKey;
import com.altamiracorp.lumify.objectDetection.DetectedObject;
import com.altamiracorp.lumify.objectDetection.ObjectDetectionWorker;
import com.altamiracorp.lumify.search.SearchProvider;
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
    private final TermMentionRepository termMentionRepository;
    private final SearchProvider searchProvider;

    private final ExecutorService executorService = MoreExecutors.getExitingExecutorService(
            new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>()),
            0L, TimeUnit.MILLISECONDS);

    @Inject
    public EntityObjectDetectionUpdate(
            final TermMentionRepository termMentionRepository,
            final ArtifactRepository artifactRepository,
            final GraphRepository graphRepository,
            final SearchProvider searchProvider) {
        this.termMentionRepository = termMentionRepository;
        this.artifactRepository = artifactRepository;
        this.graphRepository = graphRepository;
        this.searchProvider = searchProvider;
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, HandlerChain chain) throws Exception {
        EntityHelper objectDetectionHelper = new EntityHelper(termMentionRepository, graphRepository);

        User user = getUser(request);
        final String artifactRowKey = getRequiredParameter(request, "artifactKey");
        final String sign = getRequiredParameter(request, "sign");
        final String conceptId = getRequiredParameter(request, "conceptId");
        final String resolvedGraphVertexId = getRequiredParameter(request, "graphVertexId");
        final String x1 = getRequiredParameter(request, "x1");
        final String y1 = getRequiredParameter(request, "y1");
        final String x2 = getRequiredParameter(request, "x2");
        final String y2 = getRequiredParameter(request, "y2");
        String detectedObjectRowKey = getRequiredParameter(request, "detectedObjectRowKey");

        GraphVertex conceptVertex = graphRepository.findVertex(conceptId, user);
        GraphVertex resolvedVertex = graphRepository.findVertex(resolvedGraphVertexId, user);

        // update graph vertex
        resolvedVertex.setProperty(PropertyName.SUBTYPE, conceptVertex.getId());
        resolvedVertex.setProperty(PropertyName.TITLE, sign);
        graphRepository.saveVertex(resolvedVertex, user);

        // update the term mention
        TermMentionRowKey termMentionRowKey = new TermMentionRowKey(artifactRowKey, 0, 0);
        TermMention termMention = termMentionRepository.findByRowKey(termMentionRowKey.toString(), user);
        objectDetectionHelper.updateTermMention(termMention, sign, conceptVertex, resolvedVertex, user);

        // update the detected object column
        ArtifactDetectedObjects artifactDetectedObjects = artifactRepository.findByRowKey(artifactRowKey, user).getArtifactDetectedObjects();

        DetectedObject detectedObject = objectDetectionHelper.createObjectTag(x1, x2, y1, y2, resolvedVertex, conceptVertex);
        detectedObject.setRowKey(detectedObjectRowKey);

        JSONObject value = Value.toJson(artifactDetectedObjects.get(detectedObjectRowKey));
        JSONObject info = value.getJSONObject("info");
        detectedObject.setModel(info.get("model").toString());

        JSONObject obj = detectedObject.getJson();
        executorService.execute(new ObjectDetectionWorker(artifactRepository, searchProvider, artifactRowKey, detectedObjectRowKey, obj, user));

        respondWithJson(response, obj);
    }
}