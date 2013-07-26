package com.altamiracorp.reddawn.web.routes.artifact;

import com.altamiracorp.reddawn.RedDawnSession;
import com.altamiracorp.reddawn.ucd.artifact.Artifact;
import com.altamiracorp.reddawn.ucd.artifact.ArtifactRepository;
import com.altamiracorp.reddawn.ucd.artifact.ArtifactRowKey;
import com.altamiracorp.reddawn.web.WebApp;
import com.altamiracorp.web.App;
import com.altamiracorp.web.AppAware;
import com.altamiracorp.web.Handler;
import com.altamiracorp.web.HandlerChain;
import com.altamiracorp.web.utils.UrlUtils;
import org.apache.poi.util.IOUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;

public class ArtifactPosterFrameByRowKey implements Handler, AppAware {
    ArtifactRepository artifactRepository = new ArtifactRepository();
    private WebApp app;

    public static String getUrl(HttpServletRequest request, ArtifactRowKey artifactKey) {
        return UrlUtils.getRootRef(request) + "/artifact/" + UrlUtils.urlEncode(artifactKey.toString()) + "/poster-frame";
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, HandlerChain chain) throws Exception {
        RedDawnSession session = app.getRedDawnSession(request);
        ArtifactRowKey artifactKey = new ArtifactRowKey(UrlUtils.urlDecode((String) request.getAttribute("rowKey")));
        Artifact artifact = artifactRepository.findByRowKey(session.getModelSession(), artifactKey.toString());

        if (artifact == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            chain.next(request, response);
            return;
        }

        response.setContentType("image/png");
        InputStream in = artifactRepository.getRawPosterFrame(session.getModelSession(), artifact);
        try {
            IOUtils.copy(in, response.getOutputStream());
        } finally {
            in.close();
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
