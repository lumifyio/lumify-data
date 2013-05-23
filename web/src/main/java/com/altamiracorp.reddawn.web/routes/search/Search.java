package com.altamiracorp.reddawn.web.routes.search;

import com.altamiracorp.reddawn.ucd.AuthorizationLabel;
import com.altamiracorp.reddawn.ucd.UcdClient;
import com.altamiracorp.reddawn.ucd.models.Artifact;
import com.altamiracorp.reddawn.ucd.models.Term;
import com.altamiracorp.reddawn.web.WebApp;
import com.altamiracorp.reddawn.web.routes.artifact.ArtifactByRowKey;
import com.altamiracorp.reddawn.web.routes.term.TermByRowKey;
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
import java.util.List;

public class Search implements Handler, AppAware {
    private WebApp app;

  private JSONObject termsToSearchResults(List<Term> terms, HttpServletRequest request) throws JSONException {
    JSONObject termsJson = new JSONObject();
    for (Term term : terms) {
      JSONArray conceptJson = null;
      if (termsJson.has(term.getKey().getConcept())) {
        conceptJson = (JSONArray) termsJson.get(term.getKey().getConcept());
      }
      if (conceptJson == null) {
        conceptJson = new JSONArray();
        termsJson.put(term.getKey().getConcept(), conceptJson);
      }
      JSONObject termJson = new JSONObject();
      termJson.put("url", TermByRowKey.getUrl(request, term.getKey()));
      termJson.put("rowKey", term.getKey().toString());
      termJson.put("sign", term.getKey().getSign());
      termJson.put("model", term.getKey().getModel());
      conceptJson.put(termJson);
    }
    return termsJson;
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

    @Override
    public void setApp(App app) {
        this.app = (WebApp)app;
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, HandlerChain chain) throws Exception {
        UcdClient<AuthorizationLabel> client = app.getUcdClient();
        // TODO write me
        List<Artifact> artifacts = client.queryArtifactAll(app.getQueryUser());
        List<Term> terms = client.queryTermAll(app.getQueryUser());

        JSONObject result = new JSONObject();

        JSONObject termsJson = termsToSearchResults(terms, request);
        result.put("terms", termsJson);

        JSONArray artifactsJson = artifactsToSearchResults(artifacts, request);
        result.put("artifacts", artifactsJson);

        response.setContentType("application/json");
        response.getWriter().write(result.toString());
    }
}
