package com.altamiracorp.lumify.web.routes.entity;

import com.altamiracorp.lumify.core.user.User;
import com.altamiracorp.lumify.model.graph.GraphRepository;
import com.altamiracorp.lumify.model.graph.GraphVertex;
import com.altamiracorp.lumify.model.graph.InMemoryGraphVertex;
import com.altamiracorp.lumify.model.ontology.LabelName;
import com.altamiracorp.lumify.model.ontology.PropertyName;
import com.altamiracorp.lumify.model.ontology.VertexType;
import com.altamiracorp.lumify.model.termMention.TermMention;
import com.altamiracorp.lumify.model.termMention.TermMentionRepository;
import com.altamiracorp.lumify.model.termMention.TermMentionRowKey;
import com.altamiracorp.lumify.objectDetection.DetectedObject;
import com.altamiracorp.lumify.objectDetection.ObjectDetectionWorker;
import com.altamiracorp.lumify.model.search.SearchProvider;
import com.altamiracorp.lumify.ucd.artifact.Artifact;
import com.altamiracorp.lumify.ucd.artifact.ArtifactRepository;
import com.altamiracorp.lumify.web.BaseRequestHandler;
import com.altamiracorp.web.HandlerChain;
import com.google.inject.Inject;
import org.json.JSONObject;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class EntityObjectDetectionCreate extends BaseRequestHandler {
    private final GraphRepository graphRepository;
    private final ArtifactRepository artifactRepository;
    private final TermMentionRepository termMentionRepository;
    private final SearchProvider searchProvider;

    @Inject
    public EntityObjectDetectionCreate(
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
        throw new RuntimeException("storm refactor - not implemented"); // TODO storm refactor
//        EntityHelper objectDetectionHelper = new EntityHelper(termMentionRepository, graphRepository);
//
//        // required parameters
//        final String artifactRowKey = getRequiredParameter(request, "artifactKey");
//        final String artifactId = getRequiredParameter(request, "artifactId");
//        final String sign = getRequiredParameter(request, "sign");
//        final String conceptId = getRequiredParameter(request, "conceptId");
//        final String resolvedGraphVertexId = getOptionalParameter(request, "graphVertexId");
//        final JSONObject coords = new JSONObject(getRequiredParameter(request, "coords"));
//        String x1 = Double.toString(coords.getDouble("x1")), x2 = Double.toString(coords.getDouble("x2")),
//                y1 = Double.toString(coords.getDouble("y1")), y2 = Double.toString(coords.getDouble("y2"));
//        String model = getOptionalParameter(request, "model");
//        String detectedObjectRowKey = getOptionalParameter(request, "detectedObjectRowKey");
//        final String boundingBox = "[x1: " + x1 + ", y1: " + y1 +", x2: " + x2 + ", y2: " + y2 + "]";
//
//        User user = getUser(request);
//        TermMentionRowKey termMentionRowKey = new TermMentionRowKey(artifactRowKey, coords.getLong("x1"), coords.getLong("y1"));
//
//        GraphVertex conceptVertex = graphRepository.findVertex(conceptId, user);
//
//        // create new graph vertex
//        GraphVertex resolvedVertex = createGraphVertex(conceptVertex, resolvedGraphVertexId,
//                sign, termMentionRowKey.toString(), boundingBox, artifactId, user);
//
//        // create new term mention
//        TermMention termMention = new TermMention(termMentionRowKey);
//        objectDetectionHelper.updateTermMention(termMention, sign, conceptVertex, resolvedVertex, user);
//        DetectedObject detectedObject = objectDetectionHelper.createObjectTag(x1, x2, y1, y2, resolvedVertex, conceptVertex);
//
//        // create a new detected object column
//        Artifact artifact = artifactRepository.findByRowKey(artifactRowKey, user);
//
//        if (detectedObjectRowKey == null && model == null) {
//            model = "manual";
//            detectedObject.setModel(model);
//            detectedObjectRowKey = artifact.getArtifactDetectedObjects().addDetectedObject
//                    (detectedObject.getConcept(), model, x1, y1, x2, y2);
//        } else {
//            detectedObject.setModel(model);
//        }
//
//        detectedObject.setRowKey(detectedObjectRowKey);
//        JSONObject obj = detectedObject.getJson();
//        objectDetectionHelper.executeService(new ObjectDetectionWorker(artifactRepository, searchProvider, artifactRowKey, detectedObjectRowKey, obj, user));
//
//        respondWithJson(response, obj);
    }

    private GraphVertex createGraphVertex(GraphVertex conceptVertex, String resolvedGraphVertexId,
                                          String sign, String termMentionRowKey, String boundingBox, String artifactId, User user) {
        GraphVertex resolvedVertex;
        if (resolvedGraphVertexId != null) {
            resolvedVertex = graphRepository.findVertex(resolvedGraphVertexId, user);
        } else {
            resolvedVertex = graphRepository.findVertexByTitleAndType(sign, VertexType.ENTITY, user);
            if (resolvedVertex == null) {
                resolvedVertex = new InMemoryGraphVertex();
                resolvedVertex.setType(VertexType.ENTITY);
            }
            resolvedVertex.setProperty(PropertyName.ROW_KEY, termMentionRowKey);
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
