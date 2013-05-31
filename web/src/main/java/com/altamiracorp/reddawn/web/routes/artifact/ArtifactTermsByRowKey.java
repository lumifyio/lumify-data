package com.altamiracorp.reddawn.web.routes.artifact;

import com.altamiracorp.reddawn.ucd.AuthorizationLabel;
import com.altamiracorp.reddawn.ucd.UcdClient;
import com.altamiracorp.reddawn.ucd.model.artifact.ArtifactKey;
import com.altamiracorp.reddawn.ucd.model.Term;
import com.altamiracorp.reddawn.ucd.model.terms.TermMetadata;
import com.altamiracorp.reddawn.web.WebApp;
import com.altamiracorp.reddawn.web.utils.UrlUtils;
import com.altamiracorp.web.App;
import com.altamiracorp.web.AppAware;
import com.altamiracorp.web.Handler;
import com.altamiracorp.web.HandlerChain;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Collection;

public class ArtifactTermsByRowKey implements Handler, AppAware {
    private WebApp app;

  private JSONObject termToJson(Term term, ArtifactKey artifactKey) throws JSONException {
    Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
    JSONObject termJson = new JSONObject();
    termJson.put("key", new JSONObject(term.getKey().toJson()));

    JSONArray metadataJson = new JSONArray();
    for (TermMetadata termMetadata : term.getMetadata()) {
      if (artifactKey.equals(termMetadata.getArtifactKey())) {
        String termMetadataJson = gson.toJson(termMetadata);
        metadataJson.put(new JSONObject(termMetadataJson));
      }
    }
    termJson.put("metadata", metadataJson);
    return termJson;
  }

  public static String getUrl(HttpServletRequest request, ArtifactKey artifactKey) {
    return UrlUtils.getRootRef(request) + "/artifact/" + UrlUtils.urlEncode(artifactKey.toString());
  }

    @Override
    public void setApp(App app) {
        this.app = (WebApp)app;
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, HandlerChain chain) throws Exception {
        UcdClient<AuthorizationLabel> client = app.getUcdClient();
        ArtifactKey artifactKey = new ArtifactKey(UrlUtils.urlDecode((String) request.getAttribute("rowKey")));
        Collection<Term> terms = client.queryTermByArtifactKey(artifactKey, app.getQueryUser());

        JSONArray result = new JSONArray();
        for (Term term : terms) {
            JSONObject termJson = termToJson(term, artifactKey);
            result.put(termJson);
        }

        response.setContentType("application/json");
        response.getWriter().write(result.toString());
        chain.next(request, response);
    }
}
