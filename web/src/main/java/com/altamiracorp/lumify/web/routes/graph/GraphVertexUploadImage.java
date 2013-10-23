package com.altamiracorp.lumify.web.routes.graph;

import com.altamiracorp.lumify.core.model.graph.GraphRepository;
import com.altamiracorp.lumify.core.model.artifact.ArtifactRepository;
import com.altamiracorp.lumify.web.BaseRequestHandler;
import com.altamiracorp.miniweb.HandlerChain;
import com.google.inject.Inject;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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
        throw new RuntimeException("storm refactor - not implemented"); // TODO storm refactor
//        final String graphVertexId = getAttributeString(request, "graphVertexId");
//        List<Part> files = new ArrayList<Part>(request.getParts());
//        if (files.size() != 1) {
//            throw new RuntimeException("Wrong number of uploaded files. Expected 1 got " + files.size());
//        }
//        User user = getUser(request);
//        Part file = files.get(0);
//
//        String mimeType = "image";
//        if (file.getContentType() != null) {
//            mimeType = file.getContentType();
//        }
//
//        long fileSize = file.getSize();
//
//        String fileName = file.getName();
//
//        GraphVertex entityVertex = graphRepository.findVertex(graphVertexId, user);
//        if (entityVertex == null) {
//            response.sendError(HttpServletResponse.SC_NOT_FOUND);
//            return;
//        }
//
//        InputStream fileInputStream = file.getInputStream();
//        Artifact artifact = artifactRepository.createArtifactFromInputStream(
//                fileSize,
//                fileInputStream,
//                fileName,
//                new Date().getTime(),
//                user
//        );
//        artifact.getGenericMetadata().setSource("User Upload");
//        artifact.getGenericMetadata().setMimeType(mimeType);
//        artifact.getGenericMetadata().setSubject("Image of " + entityVertex.getProperty(PropertyName.TITLE));
//        artifactRepository.save(artifact, user);
//        artifact = artifactRepository.findByRowKey(artifact.getRowKey().toString(), user);
//        GraphVertex artifactVertex = null;
//        if (artifact.getGenericMetadata().getGraphVertexId() != null) {
//            artifactVertex = graphRepository.findVertex(artifact.getGenericMetadata().getGraphVertexId(), user);
//        }
//        if (artifactVertex == null) {
//            artifactVertex = artifactRepository.saveToGraph(artifact, user);
//        }
//
//        graphRepository.findOrAddRelationship(entityVertex.getId(), artifactVertex.getId(), LabelName.HAS_IMAGE, user);
//        graphRepository.commit();
//
//        entityVertex.setProperty(PropertyName.GLYPH_ICON, ArtifactThumbnailByRowKey.getUrl(artifact.getRowKey()));
//        graphRepository.commit();
//
//        respondWithJson(response, entityVertex.toJson());
    }
}

