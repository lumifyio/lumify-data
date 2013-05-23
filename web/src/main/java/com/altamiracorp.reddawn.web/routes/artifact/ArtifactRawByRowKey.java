package com.altamiracorp.reddawn.web.routes.artifact;

import com.altamiracorp.reddawn.ucd.AuthorizationLabel;
import com.altamiracorp.reddawn.ucd.UcdClient;
import com.altamiracorp.reddawn.ucd.models.Artifact;
import com.altamiracorp.reddawn.ucd.models.ArtifactKey;
import com.altamiracorp.reddawn.web.WebApp;
import com.altamiracorp.reddawn.web.utils.UrlUtils;
import com.altamiracorp.web.App;
import com.altamiracorp.web.AppAware;
import com.altamiracorp.web.Handler;
import com.altamiracorp.web.HandlerChain;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class ArtifactRawByRowKey implements Handler, AppAware {
    private WebApp app;

    public static String getUrl(HttpServletRequest request, ArtifactKey artifactKey) {
        return UrlUtils.getRootRef(request) + "/artifacts/" + UrlUtils.urlEncode(artifactKey.toString());
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, HandlerChain chain) throws Exception {
        UcdClient<AuthorizationLabel> client = app.getUcdClient();
        ArtifactKey artifactKey = new ArtifactKey(UrlUtils.urlDecode((String) request.getAttribute("rowKey")));
        Artifact artifact = client.queryArtifactByKey(artifactKey, app.getQueryUser());

        if (artifact == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            chain.next(request, response);
            return;
        }

        String mimeType = getMimeType(artifact);
        String fileName = getFileName(artifact);
        response.setContentType(mimeType);
        response.addHeader("Content-Disposition", "attachment; filename=" + fileName);
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
