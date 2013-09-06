package com.altamiracorp.reddawn.web.routes.entity;

import com.altamiracorp.reddawn.RedDawnSession;
import com.altamiracorp.reddawn.model.Column;
import com.altamiracorp.reddawn.model.GraphSession;
import com.altamiracorp.reddawn.model.Value;
import com.altamiracorp.reddawn.model.graph.GraphRepository;
import com.altamiracorp.reddawn.model.graph.GraphVertex;
import com.altamiracorp.reddawn.model.graph.InMemoryGraphVertex;
import com.altamiracorp.reddawn.model.ontology.LabelName;
import com.altamiracorp.reddawn.model.ontology.PropertyName;
import com.altamiracorp.reddawn.model.ontology.VertexType;
import com.altamiracorp.reddawn.objectDetection.DetectedObject;
import com.altamiracorp.reddawn.objectDetection.ObjectDetectionWorker;
import com.altamiracorp.reddawn.ucd.artifact.Artifact;
import com.altamiracorp.reddawn.ucd.artifact.ArtifactDetectedObjects;
import com.altamiracorp.reddawn.ucd.artifact.ArtifactRepository;
import com.altamiracorp.reddawn.web.Responder;
import com.altamiracorp.reddawn.web.WebApp;
import com.altamiracorp.web.App;
import com.altamiracorp.web.AppAware;
import com.altamiracorp.web.Handler;
import com.altamiracorp.web.HandlerChain;
import com.altamiracorp.web.utils.UrlUtils;
import com.google.common.util.concurrent.MoreExecutors;
import org.json.JSONObject;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class EntityObjectDetectionCreate implements Handler, AppAware {
    private WebApp app;
    private GraphRepository graphRepository = new GraphRepository();
    private ArtifactRepository artifactRepository = new ArtifactRepository();

    private final ExecutorService executorService = MoreExecutors.getExitingExecutorService(
            new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>()),
            0L, TimeUnit.MILLISECONDS);

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, HandlerChain chain) throws Exception {
        RedDawnSession session = app.getRedDawnSession(request);

        // required parameters
        String artifactRowKey = getRequiredParameter(request, "artifactKey");
        String artifactId = getRequiredParameter(request, "artifactId");
        String sign = getRequiredParameter(request, "sign");
        String conceptId = getRequiredParameter(request, "conceptId");
        String resolvedGraphVertexId = request.getParameter("graphVertexId");
        String x1 = getRequiredParameter(request, "x1");
        String y1 = getRequiredParameter(request, "y1");
        String x2 = getRequiredParameter(request, "x2");
        String y2 = getRequiredParameter(request, "y2");
        String boundingBox = "x1 " + x1 + ", y1 " + y1 + ", x2 " + x2 + ", y2 " + y2;
        String model = getOptionalParameter(request, "model");
        String detectedObjectRowKey = getOptionalParameter(request, "detectedObjectRowKey");

        GraphVertex conceptVertex = graphRepository.findVertex(session.getGraphSession(), conceptId);
        GraphVertex resolvedVertex = createGraphVertex(session.getGraphSession(), conceptVertex, resolvedGraphVertexId,
                sign, artifactRowKey, boundingBox, artifactId);

        DetectedObject detectedObject = new DetectedObject(x1, y1, x2, y2);
        detectedObject.setGraphVertexId(resolvedVertex.getId().toString());

        if (conceptVertex.getProperty("ontologyTitle").toString().equals("person")){
            detectedObject.setConcept("face");
        } else {
            detectedObject.setConcept(conceptVertex.getProperty("ontologyTitle").toString());
        }
        detectedObject.setResolvedVertex(resolvedVertex);

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

        new Responder(response).respondWith(obj);
    }

    @Override
    public void setApp(App app) {
        this.app = (WebApp) app;
    }

    public static String getRequiredParameter(HttpServletRequest request, String parameterName) {
        String parameter = request.getParameter(parameterName);
        if (parameter == null) {
            throw new RuntimeException("'" + parameterName + "' is required.");
        }
        return UrlUtils.urlDecode(parameter);
    }

    public static String getOptionalParameter(HttpServletRequest request, String parameterName) {
        String parameter = request.getParameter(parameterName);
        if (parameter == null) {
            return null;
        }
        return UrlUtils.urlDecode(parameter);
    }

    private GraphVertex createGraphVertex(GraphSession graphSession, GraphVertex conceptVertex, String resolvedGraphVertexId,
                                          String sign, String artifactRowKey, String boundingBox, String artifactId) {
        GraphVertex resolvedVertex;
        if (resolvedGraphVertexId != null) {
            resolvedVertex = graphRepository.findVertex(graphSession, resolvedGraphVertexId);
        } else {
            resolvedVertex = graphRepository.findVertexByTitleAndType(graphSession, sign, VertexType.ENTITY);
            if (resolvedVertex == null) {
                resolvedVertex = new InMemoryGraphVertex();
                resolvedVertex.setType(VertexType.ENTITY);
            }
            resolvedVertex.setProperty(PropertyName.ROW_KEY, artifactRowKey);
        }

        resolvedVertex.setProperty(PropertyName.BOUNDING_BOX.toString(), boundingBox);
        resolvedVertex.setProperty(PropertyName.SUBTYPE, conceptVertex.getId());
        resolvedVertex.setProperty(PropertyName.TITLE, sign);

        graphRepository.saveVertex(graphSession, resolvedVertex);

        graphRepository.saveRelationship(graphSession, artifactId, resolvedVertex.getId(), LabelName.CONTAINS_IMAGE_OF);

        return resolvedVertex;
    }
}
