package com.altamiracorp.reddawn.web.routes.artifact;

import com.altamiracorp.reddawn.search.SearchProvider;
import com.altamiracorp.reddawn.ucd.AuthorizationLabel;
import com.altamiracorp.reddawn.ucd.UcdClient;
import com.altamiracorp.reddawn.ucd.models.Artifact;
import com.altamiracorp.reddawn.ucd.models.ArtifactKey;
import com.altamiracorp.reddawn.web.WebApp;
import com.altamiracorp.web.App;
import com.altamiracorp.web.AppAware;
import com.altamiracorp.web.Handler;
import com.altamiracorp.web.HandlerChain;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ArtifactSearch implements Handler, AppAware {
    private WebApp app;

    @Override
    public void setApp(App app) {
        this.app = (WebApp) app;
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, HandlerChain chain) throws Exception {
        String query = request.getParameter("q");

        UcdClient<AuthorizationLabel> client = app.getUcdClient();
        SearchProvider SearchProvider = app.getSearchProvider();

        ArrayList<Artifact> artifacts = queryArtifacts(client, SearchProvider, query);

        JSONObject results = new JSONObject();

        JSONArray artifactsJson = artifactsToSearchResults(artifacts, request);
        results.put("document", artifactsJson); // TODO also include video and images

        response.setContentType("application/json");
        response.getWriter().write(results.toString());
    }

    private ArrayList<Artifact> queryArtifacts(UcdClient<AuthorizationLabel> client, SearchProvider searchProvider, String query) throws Exception {
        Collection<ArtifactKey> artifactKeys = searchProvider.searchArtifacts(query);
        ArrayList<Artifact> artifacts = new ArrayList<Artifact>();
        for (ArtifactKey artifactKey : artifactKeys) {
            Artifact artifact = client.queryArtifactByKey(artifactKey, app.getQueryUser());
            artifacts.add(artifact);
        }
        return artifacts;
    }

    private JSONArray artifactsToSearchResults(List<Artifact> artifacts, HttpServletRequest request) throws JSONException, UnsupportedEncodingException {
        JSONArray artifactsJson = new JSONArray();
        for (Artifact artifact : artifacts) {
            JSONObject artifactJson = new JSONObject();
            artifactJson.put("url", ArtifactByRowKey.getUrl(request, artifact.getKey()));
            artifactJson.put("rowKey", artifact.getKey().toString());
            artifactJson.put("subject", artifact.getGenericMetadata().getSubject());
            artifactsJson.put(artifactJson);
        }
        return artifactsJson;
    }
}
