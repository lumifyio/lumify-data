package com.altamiracorp.reddawn.web.routes.artifact;

import com.altamiracorp.reddawn.ucd.AuthorizationLabel;
import com.altamiracorp.reddawn.ucd.UcdClient;
import com.altamiracorp.reddawn.ucd.models.Artifact;
import com.altamiracorp.reddawn.ucd.models.ArtifactKey;
import com.altamiracorp.reddawn.web.routes.UcdServerResource;
import org.restlet.data.Status;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.ResourceException;

public class ArtifactByRowKey extends UcdServerResource {
  public Representation get() {
    try {
      UcdClient<AuthorizationLabel> client = getUcdClient();
      ArtifactKey artifactKey = new ArtifactKey(this.getAttribute("rowKey"));
      Artifact artifact = client.queryArtifactByKey(artifactKey, getQueryUser());
      if (artifact == null) {
        throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND);
      }

      return new JsonRepresentation(artifact.toJson());
    } catch (Exception ex) {
      throw new ResourceException(ex);
    }
  }
}
