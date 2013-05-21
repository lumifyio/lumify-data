package com.altamiracorp.reddawn.web;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.lesscss.LessCompiler;
import org.restlet.data.MediaType;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.ResourceException;
import org.restlet.resource.ServerResource;

import java.io.File;
import java.io.FileInputStream;

public class LessRestlet extends ServerResource {
  private static File rootDir;

  public Representation get() {
    try {
      LessCompiler lessCompiler = new LessCompiler();
      lessCompiler.setCompress(false);

      String path = getRequest().getResourceRef().toString().substring(getRequest().getRootRef().toString().length());
      File file = new File(rootDir, path);
      String fileNameWithoutExtension = file.toString().substring(0, file.toString().length() - (FilenameUtils.getExtension(file.toString()).length() + 1));
      file = new File(fileNameWithoutExtension + ".less");

      String less = IOUtils.toString(new FileInputStream(file));
      String css = lessCompiler.compile(less);

      return new StringRepresentation(css, MediaType.TEXT_CSS);
    } catch (Exception ex) {
      throw new ResourceException(ex);
    }
  }

  public static void init(File rootDir) {
    LessRestlet.rootDir = rootDir;
  }
}
