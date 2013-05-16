package com.altamiracorp.reddawn.web.routes.search;

import com.altamiracorp.reddawn.web.routes.UcdServerResource;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.ResourceException;

public class Search extends UcdServerResource {
  public Representation get() {
    try {
      // TODO write me
      return new JsonRepresentation("{\n" +
          "  \"categories\": {\n" +
          "    \"entities\": {\n" +
          "      \"person\": [\n" +
          "        {\n" +
          "          \"title\": \"Joe Ferner\",\n" +
          "          \"url\": \"http://reddawn/entities/123\"\n" +
          "        }\n" +
          "      ]\n" +
          "    },\n" +
          "    \"artifact\": [\n" +
          "      {\n" +
          "        \"title\": \"All about everything\",\n" +
          "        \"summary\": \"This is a document about everything of course\",\n" +
          "        \"url\": \"http://reddawn/artifacts/345\"\n" +
          "      }\n" +
          "    ]\n" +
          "  }\n" +
          "}");
    } catch (Exception ex) {
      throw new ResourceException(ex);
    }
  }
}
