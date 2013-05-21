package com.altamiracorp.reddawn.textExtraction;

import org.apache.commons.io.IOUtils;
import org.apache.hadoop.mapreduce.Mapper;

import java.io.IOException;
import java.io.InputStream;

public class AsciiTextExtractor implements TextExtractor {
  private static final String ASCII_MIME = "text/plain";
  private static final String nonASCII = "[^\\x00-\\x7f]";

  @Override
  public void setup(Mapper.Context context) {
  }

  @Override
  public ExtractedInfo extract(InputStream in) throws IOException {
    ExtractedInfo result = new ExtractedInfo();
    String s = IOUtils.toString(in);
    result.setText(s.replaceAll(nonASCII, ""));
    result.setMediaType(ASCII_MIME);
    return result;
  }
}
