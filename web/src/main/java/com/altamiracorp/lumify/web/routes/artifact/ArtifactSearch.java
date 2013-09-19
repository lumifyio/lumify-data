package com.altamiracorp.lumify.web.routes.artifact;

import com.altamiracorp.lumify.core.user.User;
import com.altamiracorp.lumify.search.ArtifactSearchResult;
import com.altamiracorp.lumify.search.SearchProvider;
import com.altamiracorp.lumify.web.BaseRequestHandler;
import com.altamiracorp.web.HandlerChain;
import com.google.inject.Inject;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.UnsupportedEncodingException;
import java.util.Collection;
import java.util.Date;

public class ArtifactSearch extends BaseRequestHandler {
    private SearchProvider searchProvider;

    @Inject
    public ArtifactSearch(SearchProvider searchProvider) {
        this.searchProvider = searchProvider;
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, HandlerChain chain) throws Exception {
        final String query = getRequiredParameter(request, "q");

        User user = getUser(request);
        Collection<ArtifactSearchResult> artifactSearchResults = queryArtifacts(query, user);
        JSONObject results = artifactsToSearchResults(artifactSearchResults, request);

        respondWithJson(response, results);
    }

    private Collection<ArtifactSearchResult> queryArtifacts(String query, User user) throws Exception {
        return searchProvider.searchArtifacts(query, user);
    }

    private JSONObject artifactsToSearchResults(Collection<ArtifactSearchResult> artifacts, HttpServletRequest request) throws JSONException, UnsupportedEncodingException {
        JSONObject results = new JSONObject();
        JSONArray documents = new JSONArray();
        results.put("document", documents);
        JSONArray videos = new JSONArray();
        results.put("video", videos);
        JSONArray images = new JSONArray();
        results.put("image", images);

        for (ArtifactSearchResult artifactSearchResult : artifacts) {
            JSONObject artifactJson = artifactToSearchResult(request, artifactSearchResult);
            switch (artifactSearchResult.getType()) {
                case DOCUMENT:
                    documents.put(artifactJson);
                    break;
                case VIDEO:
                    videos.put(artifactJson);
                    break;
                case IMAGE:
                    images.put(artifactJson);
                    break;
                default:
                    throw new RuntimeException("Unhandled artifact type: " + artifactSearchResult.getType());
            }
        }

        return results;
    }

    private JSONObject artifactToSearchResult(HttpServletRequest request, ArtifactSearchResult artifactSearchResult) throws JSONException {
        JSONObject artifactJson = new JSONObject();
        artifactJson.put("url", ArtifactByRowKey.getUrl(request, artifactSearchResult.getRowKey()));
        artifactJson.put("_rowKey", artifactSearchResult.getRowKey());
        artifactJson.put("subject", artifactSearchResult.getSubject());
        artifactJson.put("graphVertexId", artifactSearchResult.getGraphVertexId());
        Date publishedDate = artifactSearchResult.getPublishedDate();
        if (publishedDate != null) {
            artifactJson.put("publishedDate", publishedDate.getTime());
        }
        artifactJson.put("source", artifactSearchResult.getSource());
        return artifactJson;
    }
}
