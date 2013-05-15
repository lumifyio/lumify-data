package com.altamiracorp.reddawn.ucd.models;

import org.apache.commons.codec.binary.Hex;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class KeyHelpers {
  public static final char SEPARATOR = (char) 0x1f;

  public static String createSHA256Key(byte[] bytes) {
    try {
      MessageDigest digest = null;
      digest = MessageDigest.getInstance("SHA-256");
      return "urn" + SEPARATOR + "sha256" + SEPARATOR + Hex.encodeHexString(digest.digest(bytes));
    } catch (NoSuchAlgorithmException e) {
      throw new RuntimeException(e);
    }
  }
}
