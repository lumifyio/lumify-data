package com.altamiracorp.reddawn.web.routes.term;

import com.altamiracorp.reddawn.ucd.AuthorizationLabel;
import com.altamiracorp.reddawn.ucd.UcdClient;
import com.altamiracorp.reddawn.ucd.models.Term;
import com.altamiracorp.reddawn.ucd.models.TermKey;
import com.altamiracorp.reddawn.web.routes.UcdServerResource;
import org.restlet.Request;
import org.restlet.data.Status;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.ResourceException;

public class TermByRowKey extends UcdServerResource {
  public Representation get() {
    try {
      UcdClient<AuthorizationLabel> client = getUcdClient();
      TermKey termKey = new TermKey(urlDecode(this.getAttribute("rowKey")));
      Term term = client.queryTermByKey(termKey, getQueryUser());
      if (term == null) {
        throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND);
      }

      return new JsonRepresentation(term.toJson());
    } catch (Exception ex) {
      throw new ResourceException(ex);
    }
  }

  public static String getUrl(Request request, TermKey termKey) {
    return request.getRootRef().toString() + "/terms/" + urlEncode(termKey.toString());
  }
}
