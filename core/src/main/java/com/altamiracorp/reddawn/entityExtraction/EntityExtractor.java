package com.altamiracorp.reddawn.entityExtraction;

import com.altamiracorp.reddawn.ucd.model.ArtifactKey;
import com.altamiracorp.reddawn.ucd.model.Term;
import org.apache.hadoop.mapreduce.Mapper;

import java.io.IOException;
import java.util.Collection;

public interface EntityExtractor {
  void setup(Mapper.Context context) throws IOException;

  Collection<Term> extract(ArtifactKey artifactKey, String text) throws Exception;
}
