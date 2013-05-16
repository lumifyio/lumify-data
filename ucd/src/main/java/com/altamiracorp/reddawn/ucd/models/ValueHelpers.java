package com.altamiracorp.reddawn.ucd.models;

import org.apache.accumulo.core.data.Value;

public class ValueHelpers {
  public static Long valueToLong(Value value) {
    if (value == null) {
      return null;
    }
    return byteArrayToLong(value.get());
  }

  private static long byteArrayToLong(byte[] bytes) {
    long value = 0;
    for (int i = 0; i < bytes.length; i++) {
      value = (value << 8) + (bytes[i] & 0xff);
    }
    return value;
  }
}
