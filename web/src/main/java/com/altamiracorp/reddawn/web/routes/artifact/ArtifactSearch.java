package com.altamiracorp.reddawn.web.routes.artifact;

import com.altamiracorp.reddawn.RedDawnSession;
import com.altamiracorp.reddawn.search.ArtifactSearchResult;
import com.altamiracorp.reddawn.search.SearchProvider;
import com.altamiracorp.reddawn.web.Responder;
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
import java.util.Collection;

public class ArtifactSearch implements Handler, AppAware {
    private WebApp app;

    @Override
    public void setApp(App app) {
        this.app = (WebApp) app;
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, HandlerChain chain) throws Exception {
        String query = request.getParameter("q");
        RedDawnSession session = app.getRedDawnSession(request);
        SearchProvider searchProvider = session.getSearchProvider();
        Collection<ArtifactSearchResult> artifactSearchResults = queryArtifacts(searchProvider, query);
        JSONObject results = new JSONObject();
        JSONArray artifactsJson = artifactsToSearchResults(artifactSearchResults, request);
        results.put("document", artifactsJson); // TODO also include video and images
        new Responder(response).respondWith(results);
    }

    private Collection<ArtifactSearchResult> queryArtifacts(SearchProvider searchProvider, String query) throws Exception {
        return searchProvider.searchArtifacts(query);
    }

    private JSONArray artifactsToSearchResults(Collection<ArtifactSearchResult> artifacts, HttpServletRequest request) throws JSONException, UnsupportedEncodingException {
        JSONArray artifactsJson = new JSONArray();
        for (ArtifactSearchResult artifactSearchResult : artifacts) {
            JSONObject artifactJson = new JSONObject();
            artifactJson.put("url", ArtifactByRowKey.getUrl(request, artifactSearchResult.getRowKey()));
            artifactJson.put("rowKey", artifactSearchResult.getRowKey());
            artifactJson.put("subject", artifactSearchResult.getSubject());
            artifactsJson.put(artifactJson);
        }
        return artifactsJson;
    }
}
