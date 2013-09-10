package com.altamiracorp.lumify.web.routes.artifact;

import com.altamiracorp.lumify.AppSession;
import com.altamiracorp.lumify.model.artifactThumbnails.ArtifactThumbnail;
import com.altamiracorp.lumify.model.artifactThumbnails.ArtifactThumbnailRepository;
import com.altamiracorp.lumify.model.artifactThumbnails.ArtifactThumbnailRowKey;
import com.altamiracorp.lumify.ucd.artifact.Artifact;
import com.altamiracorp.lumify.ucd.artifact.ArtifactRepository;
import com.altamiracorp.lumify.ucd.artifact.ArtifactRowKey;
import com.altamiracorp.lumify.web.WebApp;
import com.altamiracorp.web.App;
import com.altamiracorp.web.AppAware;
import com.altamiracorp.web.Handler;
import com.altamiracorp.web.HandlerChain;
import com.altamiracorp.web.utils.UrlUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class ArtifactThumbnailByRowKey implements Handler, AppAware {
    private static final Logger LOGGER = LoggerFactory.getLogger(ArtifactThumbnailByRowKey.class.getName());
    private ArtifactRepository artifactRepository = new ArtifactRepository();
    private ArtifactThumbnailRepository artifactThumbnailRepository = new ArtifactThumbnailRepository();
    private WebApp app;

    public static String getUrl(ArtifactRowKey artifactKey) {
        return "/artifact/" + UrlUtils.urlEncode(artifactKey.toString()) + "/thumbnail";
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, HandlerChain chain) throws Exception {
        AppSession session = app.getAppSession(request);
        ArtifactRowKey artifactRowKey = new ArtifactRowKey(UrlUtils.urlDecode((String) request.getAttribute("_rowKey")));

        String widthStr = request.getParameter("width");
        int[] boundaryDims = new int[]{200, 200};
        if (widthStr != null) {
            boundaryDims[0] = boundaryDims[1] = Integer.parseInt(widthStr);
        }

        response.setContentType("image/jpeg");
        response.addHeader("Content-Disposition", "inline; filename=thumnail" + boundaryDims[0] + ".jpg");

        byte[] thumbnailData = getCachedThumbnail(session, artifactRowKey, boundaryDims);
        if (thumbnailData != null) {
            LOGGER.debug("Cache hit for: " + artifactRowKey.toString() + " " + boundaryDims[0] + "x" + boundaryDims[1]);
            ServletOutputStream out = response.getOutputStream();
            out.write(thumbnailData);
            out.close();
            return;
        }

        Artifact artifact = artifactRepository.findByRowKey(session.getModelSession(), artifactRowKey.toString());
        if (artifact == null) {
            LOGGER.warn("Cannot find artifact with row key: " + artifactRowKey.toString());
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            chain.next(request, response);
            return;
        }

        LOGGER.info("Cache miss for: " + artifactRowKey.toString() + " " + boundaryDims[0] + "x" + boundaryDims[1]);
        thumbnailData = createThumbnail(session, artifact, boundaryDims);
        ServletOutputStream out = response.getOutputStream();
        out.write(thumbnailData);
        out.close();
    }

    private byte[] createThumbnail(AppSession session, Artifact artifact, int[] boundaryDims) throws Exception {
        InputStream in = artifactRepository.getRaw(session.getModelSession(), artifact);
        try {
            BufferedImage originalImage = ImageIO.read(in);
            int[] originalImageDims = new int[]{originalImage.getWidth(), originalImage.getHeight()};
            int[] newImageDims = getScaledDimension(originalImageDims, boundaryDims);

            BufferedImage resizedImage = new BufferedImage(newImageDims[0], newImageDims[1], originalImage.getType());
            Graphics2D g = resizedImage.createGraphics();
            g.drawImage(originalImage, 0, 0, resizedImage.getWidth(), resizedImage.getHeight(), null);
            g.dispose();

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            ImageIO.write(resizedImage, "jpg", out);

            saveThumbnail(session, artifact.getRowKey(), boundaryDims, out.toByteArray());

            return out.toByteArray();
        } finally {
            in.close();
        }
    }

    private void saveThumbnail(AppSession session, ArtifactRowKey artifactRowKey, int[] boundaryDims, byte[] bytes) {
        ArtifactThumbnailRowKey artifactThumbnailRowKey = new ArtifactThumbnailRowKey(artifactRowKey.toString(), boundaryDims[0], boundaryDims[1]);
        ArtifactThumbnail artifactThumbnail = new ArtifactThumbnail(artifactThumbnailRowKey);
        artifactThumbnail.getMetadata().setData(bytes);
        artifactThumbnailRepository.save(session.getModelSession(), artifactThumbnail);
    }

    private byte[] getCachedThumbnail(AppSession session, ArtifactRowKey artifactRowKey, int[] boundaryDims) throws IOException {
        ArtifactThumbnailRowKey artifactThumbnailRowKey = new ArtifactThumbnailRowKey(artifactRowKey.toString(), boundaryDims[0], boundaryDims[1]);
        ArtifactThumbnail thumbnail = artifactThumbnailRepository.findByRowKey(session.getModelSession(), artifactThumbnailRowKey.toString());
        if (thumbnail == null) {
            return null;
        }
        return thumbnail.getMetadata().getData();
    }

    public static int[] getScaledDimension(int[] imgSize, int[] boundary) {
        int originalWidth = imgSize[0];
        int originalHeight = imgSize[1];
        int boundWidth = boundary[0];
        int boundHeight = boundary[1];
        int newWidth = originalWidth;
        int newHeight = originalHeight;

        if (originalWidth > boundWidth) {
            newWidth = boundWidth;
            newHeight = (newWidth * originalHeight) / originalWidth;
        }

        if (newHeight > boundHeight) {
            newHeight = boundHeight;
            newWidth = (newHeight * originalWidth) / originalHeight;
        }

        return new int[]{newWidth, newHeight};
    }

    @Override
    public void setApp(App app) {
        this.app = (WebApp) app;
    }
}
