package com.altamiracorp.reddawn.web.routes.artifact;

import com.altamiracorp.reddawn.ucd.AuthorizationLabel;
import com.altamiracorp.reddawn.ucd.UcdClient;
import com.altamiracorp.reddawn.ucd.models.ArtifactKey;
import com.altamiracorp.reddawn.ucd.models.Term;
import com.altamiracorp.reddawn.ucd.models.TermMetadata;
import com.altamiracorp.reddawn.web.routes.UcdServerResource;
import com.altamiracorp.web.RequestHandler;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.Request;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.ResourceException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Collection;

public class ArtifactTermsByRowKey extends UcdServerResource implements RequestHandler {
  public Representation get() {
    try {
      UcdClient<AuthorizationLabel> client = getUcdClient();
      ArtifactKey artifactKey = new ArtifactKey(urlDecode(this.getAttribute("rowKey")));
      Collection<Term> terms = client.queryTermByArtifactKey(artifactKey, getQueryUser());

      JSONArray result = new JSONArray();
      for (Term term : terms) {
        JSONObject termJson = termToJson(term, artifactKey);
        result.put(termJson);
      }

      return new JsonRepresentation(result);
    } catch (Exception ex) {
      throw new ResourceException(ex);
    }
  }

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

  public static String getUrl(Request request, ArtifactKey artifactKey) {
    return request.getRootRef().toString() + "/artifacts/" + urlEncode(artifactKey.toString());
  }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response) throws Exception {
        Representation representation = get();
        response.setContentType(representation.getMediaType().toString());
        representation.write(response.getOutputStream());
    }
}
