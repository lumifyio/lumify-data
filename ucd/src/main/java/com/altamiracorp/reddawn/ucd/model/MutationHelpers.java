package com.altamiracorp.reddawn.ucd.model;

import org.apache.accumulo.core.data.Mutation;
import org.apache.accumulo.core.data.Value;

public class MutationHelpers {
  public static void putIfNotNull(Mutation mutation, String columnFamilyName, String columnName, String value) {
    if (value != null) {
      mutation.put(columnFamilyName, columnName, value);
    }
  }

  public static void putIfNotNull(Mutation mutation, String columnFamilyName, String columnName, byte[] value) {
    if (value != null) {
      mutation.put(columnFamilyName, columnName, new Value(value));
    }
  }

  public static void putIfNotNull(Mutation mutation, String columnFamilyName, String columnName, Long value) {
    if (value != null) {
      mutation.put(columnFamilyName, columnName, longToValue(value));
    }
  }

  private static Value longToValue(Long value) {
    byte[] b = new byte[8];
    for (int i = 0; i < 8; ++i) {
      b[i] = (byte) (value >> (8 - i - 1 << 3));
    }
    return new Value(b);
  }
}
