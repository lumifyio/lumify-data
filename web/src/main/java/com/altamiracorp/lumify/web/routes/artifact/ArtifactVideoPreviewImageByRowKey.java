package com.altamiracorp.lumify.web.routes.artifact;

import com.altamiracorp.lumify.AppSession;
import com.altamiracorp.lumify.model.artifactThumbnails.ArtifactThumbnailRepository;
import com.altamiracorp.lumify.ucd.artifact.Artifact;
import com.altamiracorp.lumify.ucd.artifact.ArtifactRepository;
import com.altamiracorp.lumify.ucd.artifact.ArtifactRowKey;
import com.altamiracorp.lumify.videoPreview.ViewPreviewGenerator;
import com.altamiracorp.lumify.web.BaseRequestHandler;
import com.altamiracorp.web.HandlerChain;
import com.altamiracorp.web.utils.UrlUtils;
import org.apache.poi.util.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;

public class ArtifactVideoPreviewImageByRowKey extends BaseRequestHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(ArtifactVideoPreviewImageByRowKey.class.getName());
    private ArtifactRepository artifactRepository = new ArtifactRepository();
    private ArtifactThumbnailRepository artifactThumbnailRepository = new ArtifactThumbnailRepository();

    public static String getUrl(HttpServletRequest request, ArtifactRowKey artifactKey) {
        return UrlUtils.getRootRef(request) + "/artifact/" + UrlUtils.urlEncode(artifactKey.toString()) + "/video-preview";
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, HandlerChain chain) throws Exception {
        AppSession session = app.getAppSession(request);
        ArtifactRowKey artifactRowKey = new ArtifactRowKey(UrlUtils.urlDecode(getAttributeString(request, "_rowKey")));

        String widthStr = getOptionalParameter(request, "width");
        int[] boundaryDims = new int[]{200 * ViewPreviewGenerator.FRAMES_PER_PREVIEW, 200};

        if (widthStr != null) {
            boundaryDims[0] = Integer.parseInt(widthStr) * ViewPreviewGenerator.FRAMES_PER_PREVIEW;
            boundaryDims[1] = Integer.parseInt(widthStr);

            response.setContentType("image/jpeg");
            response.addHeader("Content-Disposition", "inline; filename=thumnail" + boundaryDims[0] + ".jpg");

            byte[] thumbnailData = artifactThumbnailRepository.getThumbnailData(session.getModelSession(), artifactRowKey, "video-preview", boundaryDims[0], boundaryDims[1]);
            if (thumbnailData != null) {
                LOGGER.debug("Cache hit for: " + artifactRowKey.toString() + " (video-preview) " + boundaryDims[0] + "x" + boundaryDims[1]);
                ServletOutputStream out = response.getOutputStream();
                out.write(thumbnailData);
                out.close();
                return;
            }
        }

        Artifact artifact = artifactRepository.findByRowKey(session.getModelSession(), artifactRowKey.toString());
        if (artifact == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            chain.next(request, response);
            return;
        }

        InputStream in = artifactRepository.getVideoPreviewImage(session.getModelSession(), artifact);
        try {
            if (widthStr != null) {
                LOGGER.info("Cache miss for: " + artifactRowKey.toString() + " (video-preview) " + boundaryDims[0] + "x" + boundaryDims[1]);
                byte[] thumbnailData = artifactThumbnailRepository.createThumbnail(session.getModelSession(), artifact.getRowKey(), "video-preview", in, boundaryDims);
                ServletOutputStream out = response.getOutputStream();
                out.write(thumbnailData);
                out.close();
            } else {
                response.setContentType("image/png");
                IOUtils.copy(in, response.getOutputStream());
            }
        } finally {
            in.close();
        }
    }
}
