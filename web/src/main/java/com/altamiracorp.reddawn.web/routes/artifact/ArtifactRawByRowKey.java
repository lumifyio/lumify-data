package com.altamiracorp.reddawn.web.routes.artifact;

import com.altamiracorp.reddawn.ucd.AuthorizationLabel;
import com.altamiracorp.reddawn.ucd.UcdClient;
import com.altamiracorp.reddawn.ucd.models.Artifact;
import com.altamiracorp.reddawn.ucd.models.ArtifactKey;
import com.altamiracorp.reddawn.web.routes.UcdServerResource;
import org.restlet.Request;
import org.restlet.data.Disposition;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.representation.ByteArrayRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.ResourceException;

public class ArtifactRawByRowKey extends UcdServerResource {
  public Representation get() {
    try {
      UcdClient<AuthorizationLabel> client = getUcdClient();
      ArtifactKey artifactKey = new ArtifactKey(urlDecode(this.getAttribute("rowKey")));
      Artifact artifact = client.queryArtifactByKey(artifactKey, getQueryUser());
      if (artifact == null) {
        throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND);
      }

      String mimeType = getMimeType(artifact);
      String fileName = getFileName(artifact);

      ByteArrayRepresentation representation = new ByteArrayRepresentation(artifact.getContent().getDocArtifactBytes(), new MediaType(mimeType));
      if (fileName != null) {
        Disposition fileNameDisposition = new Disposition(Disposition.NAME_FILENAME);
        fileNameDisposition.setFilename(fileName);
        representation.setDisposition(fileNameDisposition);
      }
      return representation;
    } catch (Exception ex) {
      throw new ResourceException(ex);
    }
  }

  private String getFileName(Artifact artifact) {
    return artifact.getGenericMetadata().getFileName() + "." + artifact.getGenericMetadata().getFileExtension();
  }

  private String getMimeType(Artifact artifact) {
    String mimeType = artifact.getGenericMetadata().getMimeType();
    if (mimeType == null || mimeType.isEmpty()) {
      mimeType = "application/octet-stream";
    }
    return mimeType;
  }

  public static String getUrl(Request request, ArtifactKey artifactKey) {
    return request.getRootRef().toString() + "/artifacts/" + urlEncode(artifactKey.toString());
  }
}
