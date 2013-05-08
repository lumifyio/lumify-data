package com.altamiracorp.reddawn.ucd.models;

import org.apache.accumulo.core.data.ColumnUpdate;

import java.util.List;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.fail;

public class MutationTestHelpers {
  public static void assertMutationContains(List<ColumnUpdate> updates, String columnFamily, String columnQualifier, String value) {
    for (ColumnUpdate update : updates) {
      if (new String(update.getColumnFamily()).equals(columnFamily)
          && new String(update.getColumnQualifier()).equals(columnQualifier)) {
        assertEquals(value, new String(update.getValue()));
        return;
      }
    }
    fail("Could not find update: " + columnFamily + ":" + columnQualifier + " = " + value);
  }
}

