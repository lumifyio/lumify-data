package com.altamiracorp.reddawn.textExtraction;

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;

public class AsciiTextExtractor implements TextExtractor {
  private static final String nonASCII = "[^\\x00-\\x7f]";

  @Override
  public ExtractedInfo extract(InputStream in) throws IOException {
    ExtractedInfo result = new ExtractedInfo();
    String s = IOUtils.toString(in);
    result.setText(s.replaceAll(nonASCII, ""));
    return result;
  }
}
