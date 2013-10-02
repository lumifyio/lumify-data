package com.altamiracorp.lumify.web.routes.entity;

import com.altamiracorp.lumify.core.user.User;
import com.altamiracorp.lumify.model.ModelSession;
import com.altamiracorp.lumify.model.Value;
import com.altamiracorp.lumify.model.graph.GraphRepository;
import com.altamiracorp.lumify.model.graph.GraphVertex;
import com.altamiracorp.lumify.model.ontology.LabelName;
import com.altamiracorp.lumify.model.ontology.PropertyName;
import com.altamiracorp.lumify.model.termMention.TermMention;
import com.altamiracorp.lumify.model.termMention.TermMentionRepository;
import com.altamiracorp.lumify.model.termMention.TermMentionRowKey;
import com.altamiracorp.lumify.objectDetection.DetectedObject;
import com.altamiracorp.lumify.objectDetection.ObjectDetectionWorker;
import com.altamiracorp.lumify.model.search.SearchProvider;
import com.altamiracorp.lumify.ucd.artifact.ArtifactRepository;
import com.altamiracorp.lumify.web.BaseRequestHandler;
import com.altamiracorp.web.HandlerChain;
import com.google.inject.Inject;
import org.json.JSONObject;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class EntityObjectDetectionUpdate extends BaseRequestHandler {
    private final GraphRepository graphRepository;
    private final ArtifactRepository artifactRepository;
    private final TermMentionRepository termMentionRepository;
    private final SearchProvider searchProvider;
    private final ModelSession modelSession;

    @Inject
    public EntityObjectDetectionUpdate(
            final TermMentionRepository termMentionRepository,
            final ArtifactRepository artifactRepository,
            final GraphRepository graphRepository,
            final SearchProvider searchProvider,
            final ModelSession modelSession) {
        this.termMentionRepository = termMentionRepository;
        this.artifactRepository = artifactRepository;
        this.graphRepository = graphRepository;
        this.searchProvider = searchProvider;
        this.modelSession = modelSession;
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, HandlerChain chain) throws Exception {
        throw new RuntimeException("storm refactor - not implemented"); // TODO storm refactor
//        EntityHelper objectDetectionHelper = new EntityHelper(termMentionRepository, graphRepository);
//
//        User user = getUser(request);
//        final String artifactRowKey = getRequiredParameter(request, "artifactKey");
//        final String sign = getRequiredParameter(request, "sign");
//        final String conceptId = getRequiredParameter(request, "conceptId");
//        final String resolvedGraphVertexId = getRequiredParameter(request, "graphVertexId");
//        final JSONObject coords = new JSONObject(getRequiredParameter(request, "coords"));
//        String x1 = Double.toString(coords.getDouble("x1")), x2 = Double.toString(coords.getDouble("x2")),
//                y1 = Double.toString(coords.getDouble("y1")), y2 = Double.toString(coords.getDouble("y2"));
//        String detectedObjectRowKey = getRequiredParameter(request, "detectedObjectRowKey");
//        final String boundingBox = "[x1: " + x1 + ", y1: " + y1 +", x2: " + x2 + ", y2: " + y2 + "]";
//
//        GraphVertex conceptVertex = graphRepository.findVertex(conceptId, user);
//        GraphVertex resolvedVertex = graphRepository.findVertex(resolvedGraphVertexId, user);
//        GraphVertex artifactVertex = graphRepository.findVertexByRowKey(artifactRowKey, user);
//
//        // update graph vertex
//        objectDetectionHelper.updateGraphVertex(resolvedVertex, conceptId, sign, user);
//        graphRepository.setPropertyEdge(artifactVertex.getId(), resolvedVertex.getId(), LabelName.CONTAINS_IMAGE_OF.toString()
//                , PropertyName.BOUNDING_BOX.toString(), boundingBox, user);
//
//        ArtifactDetectedObjects artifactDetectedObjects = artifactRepository.findByRowKey(artifactRowKey, user).getArtifactDetectedObjects();
//        JSONObject value = Value.toJson(artifactDetectedObjects.get(detectedObjectRowKey));
//        JSONObject info = value.getJSONObject("info");
//
//        // update the term mention
//        TermMentionRowKey termMentionRowKey = new TermMentionRowKey(artifactRowKey, (long)coords.getDouble("x1"), (long)coords.getDouble("y1"));
//        TermMention termMention = null;
//        if (coords.getDouble("x1") != info.getJSONObject("coords").getDouble("x1") ||  coords.getDouble("y1") != info.getJSONObject("coords").getDouble("y1")) {
//            TermMentionRowKey oldTermMentionRowKey = new TermMentionRowKey(artifactRowKey, (long)info.getJSONObject("coords").getDouble("x1"), (long)info.getJSONObject("coords").getDouble("y1"));
//            modelSession.deleteRow(TermMention.TABLE_NAME, oldTermMentionRowKey, user);
//            termMention = new TermMention(termMentionRowKey);
//        } else {
//            termMention = termMentionRepository.findByRowKey(termMentionRowKey.toString(), user);
//        }
//        objectDetectionHelper.updateTermMention(termMention, sign, conceptVertex, resolvedVertex, user);
//
//        // update the detected object column
//        DetectedObject detectedObject = objectDetectionHelper.createObjectTag(x1, x2, y1, y2, resolvedVertex, conceptVertex);
//        detectedObject.setRowKey(detectedObjectRowKey);
//
//        detectedObject.setModel(info.get("model").toString());
//
//        JSONObject obj = detectedObject.getJson();
//        objectDetectionHelper.executeService(new ObjectDetectionWorker(artifactRepository, searchProvider, artifactRowKey, detectedObjectRowKey, obj, user));
//
//        respondWithJson(response, obj);
    }
}