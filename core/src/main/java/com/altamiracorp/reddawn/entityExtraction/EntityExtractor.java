package com.altamiracorp.reddawn.entityExtraction;

import com.altamiracorp.reddawn.ucd.models.Term;
import org.apache.hadoop.mapreduce.Mapper;

import java.util.Collection;

public interface EntityExtractor {
  void setup(Mapper.Context context);

  Collection<Term> extract(String artifactKey, String text) throws Exception;
}
