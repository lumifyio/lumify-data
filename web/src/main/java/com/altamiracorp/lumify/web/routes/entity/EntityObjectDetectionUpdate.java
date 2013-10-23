package com.altamiracorp.lumify.web.routes.entity;

import com.altamiracorp.lumify.core.model.graph.GraphRepository;
import com.altamiracorp.lumify.core.model.search.SearchProvider;
import com.altamiracorp.lumify.core.model.artifact.ArtifactRepository;
import com.altamiracorp.lumify.web.BaseRequestHandler;
import com.altamiracorp.miniweb.HandlerChain;
import com.google.inject.Inject;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class EntityObjectDetectionUpdate extends BaseRequestHandler {
    private final GraphRepository graphRepository;
    private final ArtifactRepository artifactRepository;
    private final SearchProvider searchProvider;

    @Inject
    public EntityObjectDetectionUpdate(
            final ArtifactRepository artifactRepository,
            final GraphRepository graphRepository,
            final SearchProvider searchProvider) {
        this.artifactRepository = artifactRepository;
        this.graphRepository = graphRepository;
        this.searchProvider = searchProvider;
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, HandlerChain chain) throws Exception {
        throw new RuntimeException("storm refactor - not implemented"); // TODO storm refactor

//        EntityHelper objectDetectionHelper = new EntityHelper(null, graphRepository);
//
//        User user = getUser(request);
//        final String artifactRowKey = getRequiredParameter(request, "artifactKey");
//        final String sign = getRequiredParameter(request, "sign");
//        final String conceptId = getRequiredParameter(request, "conceptId");
//        final String resolvedGraphVertexId = getRequiredParameter(request, "graphVertexId");
//        final JSONObject coords = new JSONObject(getRequiredParameter(request, "coords"));
//        String x1 = Double.toString(coords.getDouble("x1")), x2 = Double.toString(coords.getDouble("x2")),
//                y1 = Double.toString(coords.getDouble("y1")), y2 = Double.toString(coords.getDouble("y2"));
//        String detectedObjectRowKey = getOptionalParameter(request, "detectedObjectRowKey");
//        final String boundingBox = "[x1: " + x1 + ", y1: " + y1 +", x2: " + x2 + ", y2: " + y2 + "]";
//
//        GraphVertex conceptVertex = graphRepository.findVertex(conceptId, user);
//        GraphVertex resolvedVertex = graphRepository.findVertex(resolvedGraphVertexId, user);
//        GraphVertex artifactVertex = graphRepository.findVertexByRowKey(artifactRowKey, user);
//
//        // update graph vertex
//        objectDetectionHelper.updateGraphVertex(resolvedVertex, conceptId, sign, user);
//
//        ArtifactDetectedObjects artifactDetectedObjects = artifactRepository.findByRowKey(artifactRowKey, user).getArtifactDetectedObjects();
//        if (detectedObjectRowKey == null) {
//            DetectedObject detectedObject = objectDetectionHelper.createObjectTag(x1, x2, y1, y2, resolvedVertex, conceptVertex);
//            detectedObject.setModel("manual");
//            detectedObjectRowKey = artifactDetectedObjects.addDetectedObject
//                    (detectedObject.getConcept(), "manual", x1, y1, x2, y2);
//            graphRepository.saveRelationship(artifactVertex.getId(), resolvedVertex.getId(), LabelName.CONTAINS_IMAGE_OF, user);
//        } else {
//            graphRepository.setPropertyEdge(artifactVertex.getId(), resolvedVertex.getId(), LabelName.CONTAINS_IMAGE_OF.toString()
//                    , PropertyName.BOUNDING_BOX.toString(), boundingBox, user);
//        }
//        JSONObject value = Value.toJson(artifactDetectedObjects.get(detectedObjectRowKey));
//        JSONObject info = value.getJSONObject("info");
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