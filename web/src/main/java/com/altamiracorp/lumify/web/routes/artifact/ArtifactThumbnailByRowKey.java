package com.altamiracorp.lumify.web.routes.artifact;

import com.altamiracorp.lumify.core.user.User;
import com.altamiracorp.lumify.model.artifactThumbnails.ArtifactThumbnailRepository;
import com.altamiracorp.lumify.ucd.artifact.Artifact;
import com.altamiracorp.lumify.ucd.artifact.ArtifactRepository;
import com.altamiracorp.lumify.ucd.artifact.ArtifactRowKey;
import com.altamiracorp.lumify.web.BaseRequestHandler;
import com.altamiracorp.web.HandlerChain;
import com.altamiracorp.web.utils.UrlUtils;
import com.google.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;

public class ArtifactThumbnailByRowKey extends BaseRequestHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(ArtifactThumbnailByRowKey.class);

    private final ArtifactRepository artifactRepository;
    private final ArtifactThumbnailRepository artifactThumbnailRepository;

    @Inject
    public ArtifactThumbnailByRowKey(final ArtifactRepository artifactRepo,
                                     final ArtifactThumbnailRepository thumbnailRepo) {
        artifactRepository = artifactRepo;
        artifactThumbnailRepository = thumbnailRepo;
    }

    public static String getUrl(ArtifactRowKey artifactKey) {
        return "/artifact/" + UrlUtils.urlEncode(artifactKey.toString()) + "/thumbnail";
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, HandlerChain chain) throws Exception {
        User user = getUser(request);
        ArtifactRowKey artifactRowKey = new ArtifactRowKey(UrlUtils.urlDecode(getAttributeString(request, "_rowKey")));

        String widthStr = getOptionalParameter(request, "width");
        int[] boundaryDims = new int[]{200, 200};
        if (widthStr != null) {
            boundaryDims[0] = boundaryDims[1] = Integer.parseInt(widthStr);
        }

        response.setContentType("image/jpeg");
        response.addHeader("Content-Disposition", "inline; filename=thumnail" + boundaryDims[0] + ".jpg");

        byte[] thumbnailData = artifactThumbnailRepository.getThumbnailData(artifactRowKey, "raw", boundaryDims[0], boundaryDims[1], user);
        if (thumbnailData != null) {
            LOGGER.debug("Cache hit for: " + artifactRowKey.toString() + " (raw) " + boundaryDims[0] + "x" + boundaryDims[1]);
            ServletOutputStream out = response.getOutputStream();
            out.write(thumbnailData);
            out.close();
            return;
        }

        Artifact artifact = artifactRepository.findByRowKey(artifactRowKey.toString(), user);
        if (artifact == null) {
            LOGGER.warn("Cannot find artifact with row key: " + artifactRowKey.toString());
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            chain.next(request, response);
            return;
        }

        LOGGER.info("Cache miss for: " + artifactRowKey.toString() + " (raw) " + boundaryDims[0] + "x" + boundaryDims[1]);
        InputStream in = artifactRepository.getRaw(artifact, user);
        try {
            thumbnailData = artifactThumbnailRepository.createThumbnail(artifact.getRowKey(), "raw", in, boundaryDims, user);
        } finally {
            in.close();
        }
        ServletOutputStream out = response.getOutputStream();
        out.write(thumbnailData);
        out.close();
    }
}
