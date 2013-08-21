package com.altamiracorp.reddawn.web.routes.graph;

import com.altamiracorp.reddawn.RedDawnSession;
import com.altamiracorp.reddawn.model.graph.GraphRepository;
import com.altamiracorp.reddawn.model.graph.GraphVertex;
import com.altamiracorp.reddawn.model.ontology.LabelName;
import com.altamiracorp.reddawn.model.ontology.PropertyName;
import com.altamiracorp.reddawn.ucd.artifact.Artifact;
import com.altamiracorp.reddawn.ucd.artifact.ArtifactRepository;
import com.altamiracorp.reddawn.web.Responder;
import com.altamiracorp.reddawn.web.WebApp;
import com.altamiracorp.reddawn.web.routes.artifact.ArtifactRawByRowKey;
import com.altamiracorp.web.App;
import com.altamiracorp.web.AppAware;
import com.altamiracorp.web.Handler;
import com.altamiracorp.web.HandlerChain;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class GraphVertexUploadImage implements Handler, AppAware {
    private ArtifactRepository artifactRepository = new ArtifactRepository();
    private GraphRepository graphRepository = new GraphRepository();
    private WebApp app;

    @Override
    public void setApp(App app) {
        this.app = (WebApp) app;
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, HandlerChain chain) throws Exception {
        RedDawnSession session = app.getRedDawnSession(request);
        String graphNodeId = (String) request.getAttribute("graphNodeId");
        List<Part> files = new ArrayList<Part>(request.getParts());
        if (files.size() != 1) {
            throw new RuntimeException("Wrong number of uploaded files. Expected 1 got " + files.size());
        }
        Part file = files.get(0);

        String mimeType = "image";
        if (file.getContentType() != null) {
            mimeType = file.getContentType();
        }

        long fileSize = file.getSize();

        String fileName = file.getName();

        GraphVertex entityVertex = graphRepository.findVertex(session.getGraphSession(), graphNodeId);
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

        entityVertex.setProperty(PropertyName.GLYPH_ICON, ArtifactRawByRowKey.getUrl(artifact.getRowKey()));

        new Responder(response).respondWith(entityVertex.toJson());
    }
}

