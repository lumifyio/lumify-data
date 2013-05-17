package com.altamiracorp.reddawn.entityExtraction;

import com.altamiracorp.reddawn.ucd.models.Term;

import java.util.ArrayList;
import java.util.Collection;

public class NullEntityExtractor implements EntityExtractor {
  @Override
  public Collection<Term> extract(String artifactKey, String text) throws Exception {
    return new ArrayList<Term>();
  }
}
