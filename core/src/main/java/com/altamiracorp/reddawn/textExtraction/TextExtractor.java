package com.altamiracorp.reddawn.textExtraction;

import org.apache.hadoop.mapreduce.Mapper;

import java.io.InputStream;

public interface TextExtractor {
  void setup(Mapper.Context context);

  ExtractedInfo extract(InputStream in) throws Exception;
}
