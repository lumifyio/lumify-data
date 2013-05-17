package com.altamiracorp.reddawn.web.routes.search;

import com.altamiracorp.reddawn.ucd.AuthorizationLabel;
import com.altamiracorp.reddawn.ucd.UcdClient;
import com.altamiracorp.reddawn.ucd.models.Artifact;
import com.altamiracorp.reddawn.ucd.models.Term;
import com.altamiracorp.reddawn.web.routes.UcdServerResource;
import com.altamiracorp.reddawn.web.routes.artifact.ArtifactByRowKey;
import com.altamiracorp.reddawn.web.routes.term.TermByRowKey;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.ResourceException;

import java.io.UnsupportedEncodingException;
import java.util.List;

public class Search extends UcdServerResource {
  public Representation get() {
    try {
      UcdClient<AuthorizationLabel> client = getUcdClient();
      // TODO write me
      List<Artifact> artifacts = client.queryArtifactAll(getQueryUser());
      List<Term> terms = client.queryTermAll(getQueryUser());

      JSONObject result = new JSONObject();

      JSONObject termsJson = termsToSearchResults(terms);
      result.put("terms", termsJson);

      JSONArray artifactsJson = artifactsToSearchResults(artifacts);
      result.put("artifacts", artifactsJson);

      return new JsonRepresentation(result);
    } catch (Exception ex) {
      throw new ResourceException(ex);
    }
  }

  private JSONObject termsToSearchResults(List<Term> terms) throws JSONException {
    JSONObject termsJson = new JSONObject();
    for (Term term : terms) {
      JSONArray conceptJson = (JSONArray) termsJson.get(term.getKey().getConcept());
      if (conceptJson == null) {
        conceptJson = new JSONArray();
        termsJson.put(term.getKey().getConcept(), conceptJson);
      }
      JSONObject termJson = new JSONObject();
      termJson.put("url", TermByRowKey.getUrl(getRequest(), term.getKey()));
      termJson.put("rowKey", term.getKey().toString());
      termJson.put("sign", term.getKey().getSign());
      termJson.put("model", term.getKey().getModel());
      conceptJson.put(termJson);
    }
    return termsJson;
  }

  private JSONArray artifactsToSearchResults(List<Artifact> artifacts) throws JSONException, UnsupportedEncodingException {
    JSONArray artifactsJson = new JSONArray();
    for (Artifact artifact : artifacts) {
      JSONObject artifactJson = new JSONObject();
      artifactJson.put("url", ArtifactByRowKey.getUrl(getRequest(), artifact.getKey()));
      artifactJson.put("rowKey", artifact.getKey().toString());
      artifactJson.put("subject", artifact.getGenericMetadata().getSubject());
      artifactsJson.put(artifactJson);
    }
    return artifactsJson;
  }
}
