package com.altamiracorp.lumify.web.routes.artifact;

import com.altamiracorp.lumify.AppSession;
import com.altamiracorp.lumify.model.artifactThumbnails.ArtifactThumbnailRepository;
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

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
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

        byte[] thumbnailData = artifactThumbnailRepository.getThumbnailData(session.getModelSession(), artifactRowKey, "raw", boundaryDims[0], boundaryDims[1]);
        if (thumbnailData != null) {
            LOGGER.debug("Cache hit for: " + artifactRowKey.toString() + " (raw) " + boundaryDims[0] + "x" + boundaryDims[1]);
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

        LOGGER.info("Cache miss for: " + artifactRowKey.toString() + " (raw) " + boundaryDims[0] + "x" + boundaryDims[1]);
        InputStream in = artifactRepository.getRaw(session.getModelSession(), artifact);
        try {
            thumbnailData = artifactThumbnailRepository.createThumbnail(session.getModelSession(), artifact.getRowKey(), "raw", in, boundaryDims);
        } finally {
            in.close();
        }
        ServletOutputStream out = response.getOutputStream();
        out.write(thumbnailData);
        out.close();
    }

    @Override
    public void setApp(App app) {
        this.app = (WebApp) app;
    }
}
