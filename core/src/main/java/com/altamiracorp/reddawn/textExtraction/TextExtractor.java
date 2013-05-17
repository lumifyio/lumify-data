package com.altamiracorp.reddawn.textExtraction;

import java.io.IOException;
import java.io.InputStream;

public interface TextExtractor {
  ExtractedInfo extract(InputStream in) throws Exception;
}
