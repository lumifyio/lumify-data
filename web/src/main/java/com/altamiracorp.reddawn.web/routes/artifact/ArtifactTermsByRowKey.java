package com.altamiracorp.reddawn.web.routes.artifact;

import com.altamiracorp.reddawn.ucd.AuthorizationLabel;
import com.altamiracorp.reddawn.ucd.UcdClient;
import com.altamiracorp.reddawn.ucd.models.ArtifactKey;
import com.altamiracorp.reddawn.ucd.models.Term;
import com.altamiracorp.reddawn.web.routes.UcdServerResource;
import org.json.JSONArray;
import org.restlet.Request;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.ResourceException;

import java.util.Collection;

public class ArtifactTermsByRowKey extends UcdServerResource {
  public Representation get() {
    try {
      UcdClient<AuthorizationLabel> client = getUcdClient();
      ArtifactKey artifactKey = new ArtifactKey(urlDecode(this.getAttribute("rowKey")));
      Collection<Term> terms = client.queryTermByArtifactKey(artifactKey, getQueryUser());

      JSONArray result = new JSONArray();
      for (Term term : terms) {
        result.put(term.toJson());
      }

      return new JsonRepresentation(result);
    } catch (Exception ex) {
      throw new ResourceException(ex);
    }
  }

  public static String getUrl(Request request, ArtifactKey artifactKey) {
    return request.getRootRef().toString() + "/artifacts/" + urlEncode(artifactKey.toString());
  }
}
