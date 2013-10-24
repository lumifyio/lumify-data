package com.altamiracorp.lumify.web.routes.entity;

import com.altamiracorp.lumify.core.ingest.ArtifactDetectedObject;
import com.altamiracorp.lumify.core.model.graph.GraphRepository;
import com.altamiracorp.lumify.core.model.graph.GraphVertex;
import com.altamiracorp.lumify.core.model.graph.InMemoryGraphVertex;
import com.altamiracorp.lumify.core.model.ontology.LabelName;
import com.altamiracorp.lumify.core.model.ontology.PropertyName;
import com.altamiracorp.lumify.core.model.ontology.VertexType;
import com.altamiracorp.lumify.core.user.User;
import com.altamiracorp.lumify.web.BaseRequestHandler;
import com.altamiracorp.miniweb.HandlerChain;
import com.google.inject.Inject;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class EntityObjectDetectionCreate extends BaseRequestHandler {
    private final GraphRepository graphRepository;
    private final EntityHelper entityHelper;

    @Inject
    public EntityObjectDetectionCreate(
            final EntityHelper entityHelper,
            final GraphRepository graphRepository) {
        this.entityHelper = entityHelper;
        this.graphRepository = graphRepository;
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, HandlerChain chain) throws Exception {

        // required parameters
        final String artifactId = getRequiredParameter(request, "artifactId");
        final String sign = getRequiredParameter(request, "sign");
        final String conceptId = getRequiredParameter(request, "conceptId");
        final JSONObject coords = new JSONObject(getRequiredParameter(request, "coords"));
        String x1 = Double.toString(coords.getDouble("x1")), x2 = Double.toString(coords.getDouble("x2")),
                y1 = Double.toString(coords.getDouble("y1")), y2 = Double.toString(coords.getDouble("y2"));
        String model = getOptionalParameter(request, "model");
        String existing = getOptionalParameter(request, "existing");
        final String boundingBox = "[x1: " + x1 + ", y1: " + y1 + ", x2: " + x2 + ", y2: " + y2 + "]";

        User user = getUser(request);

        GraphVertex conceptVertex = graphRepository.findVertex(conceptId, user);
        GraphVertex artifactVertex = graphRepository.findVertex(artifactId, user);

        // create new graph vertex
        GraphVertex resolvedVertex = createGraphVertex(conceptVertex, sign, existing, boundingBox, artifactId, user);

        ArtifactDetectedObject newDetectedObject = entityHelper.createObjectTag(x1, x2, y1, y2, resolvedVertex, conceptVertex);
        newDetectedObject.setResolvedVertex(resolvedVertex);

        // adding to detected object property if one exists, if not add detected object property to the artifact vertex
        JSONArray detectedObjectList = new JSONArray();
        if (artifactVertex.getPropertyKeys().contains(PropertyName.DETECTED_OBJECTS.toString())) {
            detectedObjectList = new JSONArray(artifactVertex.getProperty(PropertyName.DETECTED_OBJECTS).toString());
        }

        if (model == null) {
            model = "manual";
        }
        newDetectedObject.setModel(model);

        JSONObject result = newDetectedObject.getJson();
        detectedObjectList.put(newDetectedObject.getJson());
        artifactVertex.setProperty(PropertyName.DETECTED_OBJECTS, detectedObjectList.toString());
        graphRepository.saveVertex(resolvedVertex, user);

        // TODO: index the new vertex

        respondWithJson(response, result);
    }

    private GraphVertex createGraphVertex(GraphVertex conceptVertex, String sign, String existing, String boundingBox,
                                          String artifactId, User user) {
        GraphVertex resolvedVertex;
        // If the user chose to use an existing resolved entity
        if (existing != "" && existing != null) {
            resolvedVertex = graphRepository.findVertexByTitleAndType(sign, VertexType.ENTITY, user);
        } else {
            resolvedVertex = new InMemoryGraphVertex();
            resolvedVertex.setType(VertexType.ENTITY);
        }

        resolvedVertex.setProperty(PropertyName.SUBTYPE, conceptVertex.getId());
        resolvedVertex.setProperty(PropertyName.TITLE, sign);

        graphRepository.saveVertex(resolvedVertex, user);

        graphRepository.saveRelationship(artifactId, resolvedVertex.getId(), LabelName.CONTAINS_IMAGE_OF, user);
        graphRepository.setPropertyEdge(artifactId, resolvedVertex.getId(), LabelName.CONTAINS_IMAGE_OF.toString()
                , PropertyName.BOUNDING_BOX.toString(), boundingBox, user);
        return resolvedVertex;
    }
}
