package com.altamiracorp.lumify.web.routes.entity;

import com.altamiracorp.lumify.core.ingest.ArtifactDetectedObject;
import com.altamiracorp.lumify.core.model.graph.GraphRepository;
import com.altamiracorp.lumify.core.model.graph.GraphVertex;
import com.altamiracorp.lumify.core.model.ontology.LabelName;
import com.altamiracorp.lumify.core.model.ontology.PropertyName;
import com.altamiracorp.lumify.core.user.User;
import com.altamiracorp.lumify.web.BaseRequestHandler;
import com.altamiracorp.miniweb.HandlerChain;
import com.google.inject.Inject;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class EntityObjectDetectionUpdate extends BaseRequestHandler {
    private final GraphRepository graphRepository;
    private final EntityHelper entityHelper;

    @Inject
    public EntityObjectDetectionUpdate(
            final GraphRepository graphRepository,
            final EntityHelper entityHelper) {
        this.graphRepository = graphRepository;
        this.entityHelper = entityHelper;
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, HandlerChain chain) throws Exception {
        User user = getUser(request);
        final String artifactId = getRequiredParameter(request, "artifactId");
        final String sign = getRequiredParameter(request, "sign");
        final String conceptId = getRequiredParameter(request, "conceptId");
        final String resolvedGraphVertexId = getRequiredParameter(request, "graphVertexId");
        String x1 = getRequiredParameter(request, "x1"), x2 = getRequiredParameter(request, "x2"),
                y1 = getRequiredParameter(request, "y1"), y2 = getRequiredParameter(request, "y2");
        final String boundingBox = "[x1: " + x1 + ", y1: " + y1 + ", x2: " + x2 + ", y2: " + y2 + "]";

        GraphVertex conceptVertex = graphRepository.findVertex(conceptId, user);
        GraphVertex resolvedVertex = graphRepository.findVertex(resolvedGraphVertexId, user);
        GraphVertex artifactVertex = graphRepository.findVertex(artifactId, user);

        // update graph vertex
        entityHelper.updateGraphVertex(resolvedVertex, conceptId, sign, user);
        graphRepository.setPropertyEdge(artifactVertex.getId(), resolvedVertex.getId(), LabelName.CONTAINS_IMAGE_OF.toString()
                , PropertyName.BOUNDING_BOX.toString(), boundingBox, user);


        // update the detected object property on the artifact
        JSONArray detectedObjects = new JSONArray(artifactVertex.getProperty(PropertyName.DETECTED_OBJECTS).toString());
        for (int i = 0; i < detectedObjects.length(); i++) {
            JSONObject detectedObject = detectedObjects.getJSONObject(i);
            if (detectedObject.has("graphVertexId") && detectedObject.get("graphVertexId").equals(resolvedGraphVertexId)) {
                ArtifactDetectedObject tag = entityHelper.createObjectTag(x1, x2, y1, y2, resolvedVertex, conceptVertex);
                detectedObjects.put(i, tag.getJson());
                artifactVertex.setProperty(PropertyName.DETECTED_OBJECTS, detectedObjects.toString());
                graphRepository.save(artifactVertex, user);
                respondWithJson(response, tag.getJson());
                break;
            }
        }
    }
}