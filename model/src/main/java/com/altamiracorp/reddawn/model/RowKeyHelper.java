package com.altamiracorp.reddawn.model;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang.StringUtils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class RowKeyHelper {
    public static final char MINOR_FIELD_SEPARATOR = (char) 0x1f;
    public static final char MAJOR_FIELD_SEPARATOR = (char) 0x1e;

    public static String buildMinor(String... parts) {
        return StringUtils.join(parts, MINOR_FIELD_SEPARATOR);
    }

    public static String buildMajor(String... parts) {
        return StringUtils.join(parts, MAJOR_FIELD_SEPARATOR);
    }

    public static String buildSHA256KeyString(byte[] bytes) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] sha = digest.digest(bytes);
            return "urn" + MINOR_FIELD_SEPARATOR + "sha256" + MINOR_FIELD_SEPARATOR + Hex.encodeHexString(sha);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public static String[] split(String rowKey) {
        return rowKey.split("" + MINOR_FIELD_SEPARATOR);
    }
}
