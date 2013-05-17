package com.altamiracorp.reddawn.ucd.models;

import org.apache.commons.codec.binary.Hex;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class KeyHelpers {
  public static final char SEPARATOR = (char) 0x1f;

  public static String createSHA256Key(byte[] bytes) {
    try {
      MessageDigest digest = MessageDigest.getInstance("SHA-256");
      byte[] sha = digest.digest(bytes);
      return "urn" + SEPARATOR + "sha256" + SEPARATOR + Hex.encodeHexString(sha);
    } catch (NoSuchAlgorithmException e) {
      throw new RuntimeException(e);
    }
  }

  public static String createSHA256Key(String str) {
    return createSHA256Key(str.getBytes());
  }

  public static String createCompositeKey(String... parts) {
    StringBuilder result = new StringBuilder();
    for (int i = 0; i < parts.length; i++) {
      if (i != 0) {
        result.append(SEPARATOR);
      }
      result.append(parts[i]);
    }
    return result.toString();
  }

  public static String createSHA256KeyFromConcatination(String[] strings) {
    StringBuilder sb = new StringBuilder();
    for (String str : strings) {
      if (str != null) {
        sb.append(str);
      }
    }
    return createSHA256Key(sb.toString());
  }

  public static String[] splitOnSeperator(String keyString) {
    return keyString.split("" + SEPARATOR);
  }
}
