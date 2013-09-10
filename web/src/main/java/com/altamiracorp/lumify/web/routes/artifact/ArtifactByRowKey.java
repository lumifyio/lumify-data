package com.altamiracorp.lumify.web.routes.artifact;

import com.altamiracorp.lumify.AppSession;
import com.altamiracorp.lumify.ucd.artifact.Artifact;
import com.altamiracorp.lumify.ucd.artifact.ArtifactRepository;
import com.altamiracorp.lumify.ucd.artifact.ArtifactRowKey;
import com.altamiracorp.lumify.ucd.artifact.ArtifactType;
import com.altamiracorp.lumify.web.Responder;
import com.altamiracorp.lumify.web.WebApp;
import com.altamiracorp.web.App;
import com.altamiracorp.web.AppAware;
import com.altamiracorp.web.Handler;
import com.altamiracorp.web.HandlerChain;
import com.altamiracorp.web.utils.UrlUtils;
import org.json.JSONObject;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class ArtifactByRowKey implements Handler, AppAware {
    private ArtifactRepository artifactRepository = new ArtifactRepository();
    private WebApp app;

    public static String getUrl(HttpServletRequest request, String artifactKey) {
        return UrlUtils.getRootRef(request) + "/artifact/" + UrlUtils.urlEncode(artifactKey.toString());
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, HandlerChain chain) throws Exception {
        AppSession session = app.getAppSession(request);
        ArtifactRowKey artifactKey = new ArtifactRowKey(UrlUtils.urlDecode((String) request.getAttribute("_rowKey")));
        Artifact artifact = artifactRepository.findByRowKey(session.getModelSession(), artifactKey.toString());

        if (artifact == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        } else {
            JSONObject artifactJson = artifact.toJson();
            artifactJson.put("rawUrl", ArtifactRawByRowKey.getUrl(artifact.getRowKey()));
            artifactJson.put("thumbnailUrl", ArtifactThumbnailByRowKey.getUrl(artifact.getRowKey()));
            artifactJson.put("source", artifact.getGenericMetadata().getSource());
            if (artifact.getType() == ArtifactType.VIDEO) {
                artifactJson.put("posterFrameUrl", ArtifactPosterFrameByRowKey.getUrl(request, artifact.getRowKey()));
                artifactJson.put("videoPreviewImageUrl", ArtifactVideoPreviewImageByRowKey.getUrl(request, artifact.getRowKey()));
            }
            new Responder(response).respondWith(artifactJson);
        }

        chain.next(request, response);
    }

    @Override
    public void setApp(App app) {
        this.app = (WebApp) app;
    }

    public void setArtifactRepository(ArtifactRepository artifactRepository) {
        this.artifactRepository = artifactRepository;
    }
}
