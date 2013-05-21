package com.altamiracorp.reddawn.web.routes.artifact;

import com.altamiracorp.reddawn.ucd.AuthorizationLabel;
import com.altamiracorp.reddawn.ucd.UcdClient;
import com.altamiracorp.reddawn.ucd.models.Artifact;
import com.altamiracorp.reddawn.ucd.models.ArtifactKey;
import com.altamiracorp.reddawn.web.routes.UcdServerResource;
import com.altamiracorp.web.RequestHandler;
import org.restlet.Request;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.ResourceException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class ArtifactTextByRowKey extends UcdServerResource implements RequestHandler {
  public Representation get() {
    try {
      UcdClient<AuthorizationLabel> client = getUcdClient();
      ArtifactKey artifactKey = new ArtifactKey(urlDecode(this.getAttribute("rowKey")));
      Artifact artifact = client.queryArtifactByKey(artifactKey, getQueryUser());
      if (artifact == null) {
        throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND);
      }

      return new StringRepresentation(artifact.getContent().getDocExtractedText(), MediaType.TEXT_PLAIN);
    } catch (Exception ex) {
      throw new ResourceException(ex);
    }
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
