package com.altamiracorp.reddawn.web.routes.search;

import com.altamiracorp.reddawn.ucd.AuthorizationLabel;
import com.altamiracorp.reddawn.ucd.UcdClient;
import com.altamiracorp.reddawn.ucd.models.Artifact;
import com.altamiracorp.reddawn.web.routes.UcdServerResource;
import com.altamiracorp.reddawn.web.routes.artifact.ArtifactByRowKey;
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

      JSONObject result = new JSONObject();
      JSONObject categoriesJson = new JSONObject();
      result.put("categories", categoriesJson);

      JSONObject entitiesJson = new JSONObject("{\"person\": [\n" +
          "        {\n" +
          "          \"title\": \"Joe Ferner\",\n" +
          "          \"url\": \"http://reddawn/entities/123\"\n" +
          "        }\n" +
          "      ]}");
      categoriesJson.put("entities", entitiesJson);

      JSONArray artifactsJson = toSearchResults(artifacts);
      categoriesJson.put("artifacts", artifactsJson);

      return new JsonRepresentation(result);
    } catch (Exception ex) {
      throw new ResourceException(ex);
    }
  }

  private JSONArray toSearchResults(List<Artifact> artifacts) throws JSONException, UnsupportedEncodingException {
    JSONArray artifactsJson = new JSONArray();
    for (Artifact artifact : artifacts) {
      JSONObject artifactJson = new JSONObject();
      artifactJson.put("url", ArtifactByRowKey.getUrl(getRequest(), artifact.getKey()));
      artifactJson.put("subject", artifact.getGenericMetadata().getSubject());
      artifactsJson.put(artifactJson);
    }
    return artifactsJson;
  }
}
