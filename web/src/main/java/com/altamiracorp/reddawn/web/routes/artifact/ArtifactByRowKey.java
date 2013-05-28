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
import org.json.JSONObject;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class ArtifactByRowKey implements Handler, AppAware {
    private WebApp app;

    public static String getUrl(HttpServletRequest request, ArtifactKey artifactKey) {
        return UrlUtils.getRootRef(request) + "/artifact/" + UrlUtils.urlEncode(artifactKey.toString());
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, HandlerChain chain) throws Exception {
        UcdClient<AuthorizationLabel> client = app.getUcdClient();
        ArtifactKey artifactKey = new ArtifactKey(UrlUtils.urlDecode((String) request.getAttribute("rowKey")));
        Artifact artifact = client.queryArtifactByKey(artifactKey, app.getQueryUser());

        if (artifact == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        } else {
            response.setContentType("application/json");
            JSONObject artifactJson = new JSONObject(artifact.toJson());
            artifactJson.put("rawUrl", ArtifactRawByRowKey.getUrl(request, artifact.getKey()));
            response.getWriter().write(artifactJson.toString());
        }

        chain.next(request, response);
    }

    @Override
    public void setApp(App app) {
        this.app = (WebApp) app;
    }
}
