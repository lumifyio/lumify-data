package com.altamiracorp.lumify.web.routes.artifact;

import com.altamiracorp.lumify.core.user.User;
import com.altamiracorp.lumify.ucd.artifact.Artifact;
import com.altamiracorp.lumify.ucd.artifact.ArtifactRepository;
import com.altamiracorp.lumify.ucd.artifact.ArtifactRowKey;
import com.altamiracorp.lumify.ucd.artifact.ArtifactType;
import com.altamiracorp.lumify.web.BaseRequestHandler;
import com.altamiracorp.web.HandlerChain;
import com.altamiracorp.web.utils.UrlUtils;
import com.google.inject.Inject;
import org.json.JSONObject;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class ArtifactByRowKey extends BaseRequestHandler {
    private final ArtifactRepository artifactRepository;

    @Inject
    public ArtifactByRowKey(final ArtifactRepository artifactRepository) {
        this.artifactRepository = artifactRepository;
    }

    public static String getUrl(HttpServletRequest request, String artifactKey) {
        return UrlUtils.getRootRef(request) + "/artifact/" + UrlUtils.urlEncode(artifactKey);
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, HandlerChain chain) throws Exception {
        throw new RuntimeException("storm refactor - not implemented"); // TODO storm refactor
//        User user = getUser(request);
//        ArtifactRowKey artifactKey = new ArtifactRowKey(UrlUtils.urlDecode(getAttributeString(request, "_rowKey")));
//        Artifact artifact = artifactRepository.findByRowKey(artifactKey.toString(), user);
//
//        if (artifact == null) {
//            response.sendError(HttpServletResponse.SC_NOT_FOUND);
//        } else {
//            JSONObject artifactJson = artifact.toJson();
//            artifactJson.put("rawUrl", ArtifactRawByRowKey.getUrl(artifact.getRowKey()));
//            artifactJson.put("thumbnailUrl", ArtifactThumbnailByRowKey.getUrl(artifact.getRowKey()));
//            artifactJson.put("source", artifact.getGenericMetadata().getSource());
//            if (artifact.getType() == ArtifactType.VIDEO) {
//                artifactJson.put("posterFrameUrl", ArtifactPosterFrameByRowKey.getUrl(request, artifact.getRowKey()));
//                artifactJson.put("videoPreviewImageUrl", ArtifactVideoPreviewImageByRowKey.getUrl(request, artifact.getRowKey()));
//            }
//            respondWithJson(response, artifactJson);
//        }
//
//        chain.next(request, response);
    }
}
