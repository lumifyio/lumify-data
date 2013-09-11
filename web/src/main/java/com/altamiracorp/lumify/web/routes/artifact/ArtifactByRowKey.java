package com.altamiracorp.lumify.web.routes.artifact;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONObject;

import com.altamiracorp.lumify.AppSession;
import com.altamiracorp.lumify.ucd.artifact.Artifact;
import com.altamiracorp.lumify.ucd.artifact.ArtifactRepository;
import com.altamiracorp.lumify.ucd.artifact.ArtifactRowKey;
import com.altamiracorp.lumify.ucd.artifact.ArtifactType;
import com.altamiracorp.lumify.web.BaseRequestHandler;
import com.altamiracorp.web.HandlerChain;
import com.altamiracorp.web.utils.UrlUtils;

public class ArtifactByRowKey extends BaseRequestHandler {
    private ArtifactRepository artifactRepository = new ArtifactRepository();

    public static String getUrl(HttpServletRequest request, String artifactKey) {
        return UrlUtils.getRootRef(request) + "/artifact/" + UrlUtils.urlEncode(artifactKey.toString());
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, HandlerChain chain) throws Exception {
        AppSession session = app.getAppSession(request);
        ArtifactRowKey artifactKey = new ArtifactRowKey(UrlUtils.urlDecode(getAttributeString(request, "_rowKey")));
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
            respondWithJson(response, artifactJson);
        }

        chain.next(request, response);
    }

    public void setArtifactRepository(ArtifactRepository artifactRepository) {
        this.artifactRepository = artifactRepository;
    }
}
