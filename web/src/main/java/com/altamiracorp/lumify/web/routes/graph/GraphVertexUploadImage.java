package com.altamiracorp.lumify.web.routes.graph;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

import com.altamiracorp.lumify.AppSession;
import com.altamiracorp.lumify.model.graph.GraphRepository;
import com.altamiracorp.lumify.model.graph.GraphVertex;
import com.altamiracorp.lumify.model.ontology.LabelName;
import com.altamiracorp.lumify.model.ontology.PropertyName;
import com.altamiracorp.lumify.ucd.artifact.Artifact;
import com.altamiracorp.lumify.ucd.artifact.ArtifactRepository;
import com.altamiracorp.lumify.web.BaseRequestHandler;
import com.altamiracorp.lumify.web.routes.artifact.ArtifactThumbnailByRowKey;
import com.altamiracorp.web.HandlerChain;

public class GraphVertexUploadImage extends BaseRequestHandler {
    private ArtifactRepository artifactRepository = new ArtifactRepository();
    private GraphRepository graphRepository = new GraphRepository();

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, HandlerChain chain) throws Exception {
        final String graphVertexId = getAttributeString(request, "graphVertexId");
        List<Part> files = new ArrayList<Part>(request.getParts());
        if (files.size() != 1) {
            throw new RuntimeException("Wrong number of uploaded files. Expected 1 got " + files.size());
        }
        AppSession session = app.getAppSession(request);
        Part file = files.get(0);

        String mimeType = "image";
        if (file.getContentType() != null) {
            mimeType = file.getContentType();
        }

        long fileSize = file.getSize();

        String fileName = file.getName();

        GraphVertex entityVertex = graphRepository.findVertex(session.getGraphSession(), graphVertexId);
        if (entityVertex == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        InputStream fileInputStream = file.getInputStream();
        Artifact artifact = artifactRepository.createArtifactFromInputStream(
                session.getModelSession(),
                fileSize,
                fileInputStream,
                fileName,
                new Date().getTime()
        );
        artifact.getGenericMetadata().setSource("User Upload");
        artifact.getGenericMetadata().setMimeType(mimeType);
        artifact.getGenericMetadata().setSubject("Image of " + entityVertex.getProperty(PropertyName.TITLE));
        artifactRepository.save(session.getModelSession(), artifact);
        artifact = artifactRepository.findByRowKey(session.getModelSession(), artifact.getRowKey().toString());
        GraphVertex artifactVertex = null;
        if (artifact.getGenericMetadata().getGraphVertexId() != null) {
            artifactVertex = graphRepository.findVertex(session.getGraphSession(), artifact.getGenericMetadata().getGraphVertexId());
        }
        if (artifactVertex == null) {
            artifactVertex = artifactRepository.saveToGraph(session.getModelSession(), session.getGraphSession(), artifact);
        }

        graphRepository.findOrAddRelationship(session.getGraphSession(), entityVertex.getId(), artifactVertex.getId(), LabelName.HAS_IMAGE);

        entityVertex.setProperty(PropertyName.GLYPH_ICON, ArtifactThumbnailByRowKey.getUrl(artifact.getRowKey()));

        respondWithJson(response, entityVertex.toJson());
    }
}

