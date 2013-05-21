package com.altamiracorp.reddawn.web;

import com.altamiracorp.web.RequestHandler;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.lesscss.LessCompiler;
import org.restlet.data.MediaType;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.ResourceException;
import org.restlet.resource.ServerResource;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;

public class LessRestlet extends ServerResource implements RequestHandler {
  private static File rootDir;

  public Representation get() {
    try {
      LessCompiler lessCompiler = new LessCompiler();
      lessCompiler.setCompress(false);

      String path = getRequest().getResourceRef().toString().substring(getRequest().getRootRef().toString().length());
      File file = new File(rootDir, path);
      String fileNameWithoutExtension = file.toString().substring(0, file.toString().length() - (FilenameUtils.getExtension(file.toString()).length() + 1));
      file = new File(fileNameWithoutExtension + ".less");

      String css = lessCompiler.compile(file);

      return new StringRepresentation(css, MediaType.TEXT_CSS);
    } catch (Exception ex) {
      throw new ResourceException(ex);
    }
  }

  public static void init(File rootDir) {
    LessRestlet.rootDir = rootDir;
  }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response) throws Exception {
        Representation representation = get();
        response.setContentType(representation.getMediaType().toString());
        representation.write(response.getOutputStream());
    }
}
