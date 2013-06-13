package com.altamiracorp.reddawn.ucd.predicate;

import com.altamiracorp.reddawn.model.RowKey;
import com.altamiracorp.reddawn.model.RowKeyHelper;

public class PredicateRowKey extends RowKey {
  public PredicateRowKey(String modelKey, String predicateLabel) {
    super(RowKeyHelper.buildMinor(modelKey, predicateLabel).toString());
  }
}
