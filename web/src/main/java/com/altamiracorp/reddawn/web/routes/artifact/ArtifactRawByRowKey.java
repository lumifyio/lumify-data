package com.altamiracorp.reddawn.web.routes.artifact;

import com.altamiracorp.reddawn.RedDawnSession;
import com.altamiracorp.reddawn.ucd.artifact.Artifact;
import com.altamiracorp.reddawn.ucd.artifact.ArtifactRepository;
import com.altamiracorp.reddawn.ucd.artifact.ArtifactRowKey;
import com.altamiracorp.reddawn.web.WebApp;
import com.altamiracorp.reddawn.web.utils.UrlUtils;
import com.altamiracorp.web.App;
import com.altamiracorp.web.AppAware;
import com.altamiracorp.web.Handler;
import com.altamiracorp.web.HandlerChain;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class ArtifactRawByRowKey implements Handler, AppAware {
    ArtifactRepository artifactRepository = new ArtifactRepository();
    private WebApp app;

    public static String getUrl(HttpServletRequest request, ArtifactRowKey artifactKey) {
        return UrlUtils.getRootRef(request) + "/artifact/" + UrlUtils.urlEncode(artifactKey.toString()) + "/raw";
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, HandlerChain chain) throws Exception {
        boolean download = request.getParameter("download") != null;

        RedDawnSession session = app.getRedDawnSession(request);
        ArtifactRowKey artifactKey = new ArtifactRowKey(UrlUtils.urlDecode((String) request.getAttribute("rowKey")));
        Artifact artifact = artifactRepository.findByRowKey(session.getModelSession(), artifactKey.toString());

        if (artifact == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            chain.next(request, response);
            return;
        }

        String mimeType = getMimeType(artifact);
        String fileName = getFileName(artifact);
        response.setContentType(mimeType);
        if (download) {
            response.addHeader("Content-Disposition", "attachment; filename=" + fileName);
        } else {
            response.addHeader("Content-Disposition", "inline; filename=" + fileName);
        }
        response.getOutputStream().write(artifact.getContent().getDocArtifactBytes());
        chain.next(request, response);
    }

    @Override
    public void setApp(App app) {
        this.app = (WebApp) app;
    }

    private String getFileName(Artifact artifact) {
        return artifact.getGenericMetadata().getFileName() + "." + artifact.getGenericMetadata().getFileExtension();
    }

    private String getMimeType(Artifact artifact) {
        String mimeType = artifact.getGenericMetadata().getMimeType();
        if (mimeType == null || mimeType.isEmpty()) {
            mimeType = "application/octet-stream";
        }
        return mimeType;
    }
}
