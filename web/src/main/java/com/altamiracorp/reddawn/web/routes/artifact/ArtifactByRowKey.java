package com.altamiracorp.reddawn.web.routes.artifact;

import com.altamiracorp.reddawn.RedDawnSession;
import com.altamiracorp.reddawn.ucd.artifact.Artifact;
import com.altamiracorp.reddawn.ucd.artifact.ArtifactRepository;
import com.altamiracorp.reddawn.ucd.artifact.ArtifactRowKey;
import com.altamiracorp.reddawn.ucd.artifact.ArtifactType;
import com.altamiracorp.reddawn.web.Responder;
import com.altamiracorp.reddawn.web.WebApp;
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
        RedDawnSession session = app.getRedDawnSession(request);
        ArtifactRowKey artifactKey = new ArtifactRowKey(UrlUtils.urlDecode((String) request.getAttribute("rowKey")));
        Artifact artifact = artifactRepository.findByRowKey(session.getModelSession(), artifactKey.toString());

        if (artifact == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        } else {
            JSONObject artifactJson = artifact.toJson();
            artifactJson.put("rawUrl", ArtifactRawByRowKey.getUrl(request, artifact.getRowKey()));
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
