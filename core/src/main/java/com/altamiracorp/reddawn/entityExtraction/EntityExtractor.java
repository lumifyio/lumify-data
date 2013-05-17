package com.altamiracorp.reddawn.entityExtraction;

import com.altamiracorp.reddawn.ucd.models.Term;

import java.util.Collection;

public interface EntityExtractor {
  Collection<Term> extract(String artifactKey, String text) throws Exception;
}
