package com.altamiracorp.reddawn.entityExtraction;

import com.altamiracorp.reddawn.ucd.models.Term;
import org.apache.hadoop.mapreduce.Mapper;

import java.util.ArrayList;
import java.util.Collection;

public class NullEntityExtractor implements EntityExtractor {
  @Override
  public void setup(Mapper.Context context) {
  }

  @Override
  public Collection<Term> extract(String artifactKey, String text) throws Exception {
    return new ArrayList<Term>();
  }
}
