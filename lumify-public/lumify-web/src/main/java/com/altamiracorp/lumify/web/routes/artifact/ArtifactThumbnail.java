package com.altamiracorp.lumify.web.routes.artifact;

import com.altamiracorp.lumify.core.model.artifactThumbnails.ArtifactThumbnailRepository;
import com.altamiracorp.lumify.core.model.ontology.PropertyName;
import com.altamiracorp.lumify.core.user.User;
import com.altamiracorp.lumify.core.util.LumifyLogger;
import com.altamiracorp.lumify.core.util.LumifyLoggerFactory;
import com.altamiracorp.lumify.web.BaseRequestHandler;
import com.altamiracorp.miniweb.HandlerChain;
import com.altamiracorp.miniweb.utils.UrlUtils;
import com.altamiracorp.securegraph.Graph;
import com.altamiracorp.securegraph.Vertex;
import com.altamiracorp.securegraph.property.StreamingPropertyValue;
import com.google.inject.Inject;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;

public class ArtifactThumbnail extends BaseRequestHandler {
    private static final LumifyLogger LOGGER = LumifyLoggerFactory.getLogger(ArtifactThumbnail.class);

    private final ArtifactThumbnailRepository artifactThumbnailRepository;
    private final Graph graph;

    @Inject
    public ArtifactThumbnail(
            final ArtifactThumbnailRepository thumbnailRepo,
            final Graph graph) {
        artifactThumbnailRepository = thumbnailRepo;
        this.graph = graph;
    }

    public static String getUrl(Object graphVertexId) {
        return "/artifact/" + graphVertexId + "/thumbnail";
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, HandlerChain chain) throws Exception {
        User user = getUser(request);
        String graphVertexId = UrlUtils.urlDecode(getAttributeString(request, "graphVertexId"));

        Vertex artifactVertex = graph.getVertex(graphVertexId, user.getAuthorizations());
        if (artifactVertex == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            chain.next(request, response);
            return;
        }

        String widthStr = getOptionalParameter(request, "width");
        int[] boundaryDims = new int[]{200, 200};
        if (widthStr != null) {
            boundaryDims[0] = boundaryDims[1] = Integer.parseInt(widthStr);
        }

        byte[] thumbnailData;
        com.altamiracorp.lumify.core.model.artifactThumbnails.ArtifactThumbnail thumbnail = artifactThumbnailRepository.getThumbnail(artifactVertex.getId(), "raw", boundaryDims[0], boundaryDims[1], user);
        if (thumbnail != null) {
            String format = thumbnail.getMetadata().getFormat();
            response.setContentType("image/" + format);
            response.addHeader("Content-Disposition", "inline; filename=thumbnail" + boundaryDims[0] + "." + format);

            thumbnailData = thumbnail.getMetadata().getData();
            if (thumbnailData != null) {
                LOGGER.debug("Cache hit for: %s (raw) %d x %d", artifactVertex.getId().toString(), boundaryDims[0], boundaryDims[1]);
                ServletOutputStream out = response.getOutputStream();
                out.write(thumbnailData);
                out.close();
                return;
            }
        }

        LOGGER.info("Cache miss for: %s (raw) %d x %d", artifactVertex.getId().toString(), boundaryDims[0], boundaryDims[1]);
        StreamingPropertyValue rawPropertyValue = (StreamingPropertyValue) artifactVertex.getPropertyValue(PropertyName.RAW.toString(), 0);
        if (rawPropertyValue == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            chain.next(request, response);
            return;
        }

        InputStream in = rawPropertyValue.getInputStream(user.getAuthorizations());
        try {
            thumbnail = artifactThumbnailRepository.createThumbnail(artifactVertex.getId(), "raw", in, boundaryDims, user);

            String format = thumbnail.getMetadata().getFormat();
            response.setContentType("image/" + format);
            response.addHeader("Content-Disposition", "inline; filename=thumbnail" + boundaryDims[0] + "." + format);

            thumbnailData = thumbnail.getMetadata().getData();
        } finally {
            in.close();
        }
        ServletOutputStream out = response.getOutputStream();
        out.write(thumbnailData);
        out.close();
    }
}
