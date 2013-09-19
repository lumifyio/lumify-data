package com.altamiracorp.lumify.web.routes.graph;

import com.altamiracorp.lumify.core.user.User;
import com.altamiracorp.lumify.model.graph.GraphRepository;
import com.altamiracorp.lumify.model.graph.GraphVertex;
import com.altamiracorp.lumify.model.ontology.LabelName;
import com.altamiracorp.lumify.model.ontology.PropertyName;
import com.altamiracorp.lumify.ucd.artifact.Artifact;
import com.altamiracorp.lumify.ucd.artifact.ArtifactRepository;
import com.altamiracorp.lumify.web.BaseRequestHandler;
import com.altamiracorp.lumify.web.routes.artifact.ArtifactThumbnailByRowKey;
import com.altamiracorp.web.HandlerChain;
import com.google.inject.Inject;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class GraphVertexUploadImage extends BaseRequestHandler {
    private final ArtifactRepository artifactRepository;
    private final GraphRepository graphRepository;

    @Inject
    public GraphVertexUploadImage(final ArtifactRepository artifactRepo, final GraphRepository graphRepo) {
        artifactRepository = artifactRepo;
        graphRepository = graphRepo;
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, HandlerChain chain) throws Exception {
        final String graphVertexId = getAttributeString(request, "graphVertexId");
        List<Part> files = new ArrayList<Part>(request.getParts());
        if (files.size() != 1) {
            throw new RuntimeException("Wrong number of uploaded files. Expected 1 got " + files.size());
        }
        User user = getUser(request);
        Part file = files.get(0);

        String mimeType = "image";
        if (file.getContentType() != null) {
            mimeType = file.getContentType();
        }

        long fileSize = file.getSize();

        String fileName = file.getName();

        GraphVertex entityVertex = graphRepository.findVertex(graphVertexId, user);
        if (entityVertex == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        InputStream fileInputStream = file.getInputStream();
        Artifact artifact = artifactRepository.createArtifactFromInputStream(
                fileSize,
                fileInputStream,
                fileName,
                new Date().getTime(),
                user
        );
        artifact.getGenericMetadata().setSource("User Upload");
        artifact.getGenericMetadata().setMimeType(mimeType);
        artifact.getGenericMetadata().setSubject("Image of " + entityVertex.getProperty(PropertyName.TITLE));
        artifactRepository.save(artifact, user);
        artifact = artifactRepository.findByRowKey(artifact.getRowKey().toString(), user);
        GraphVertex artifactVertex = null;
        if (artifact.getGenericMetadata().getGraphVertexId() != null) {
            artifactVertex = graphRepository.findVertex(artifact.getGenericMetadata().getGraphVertexId(), user);
        }
        if (artifactVertex == null) {
            artifactVertex = artifactRepository.saveToGraph(artifact, user);
        }

        graphRepository.findOrAddRelationship(entityVertex.getId(), artifactVertex.getId(), LabelName.HAS_IMAGE, user);
        graphRepository.commit();

        entityVertex.setProperty(PropertyName.GLYPH_ICON, ArtifactThumbnailByRowKey.getUrl(artifact.getRowKey()));
        graphRepository.commit();

        respondWithJson(response, entityVertex.toJson());
    }
}

