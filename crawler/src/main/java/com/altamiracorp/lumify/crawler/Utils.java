package com.altamiracorp.lumify.crawler;

import org.apache.commons.codec.binary.Hex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

public class Utils {
    private static final Logger LOGGER = LoggerFactory.getLogger(Utils.class);

    /**
     * Concatenates the strings in the list, separated by the connector specified
     *
     * @param list      Terms to concatenate
     * @param connector String to insert in between each value
     * @return Concatenated string of terms
     */
    public static String concatenate(ArrayList<String> list, String connector) {
        String ret = "";
        for (String entry : list) {
            if (ret.length() > 0) ret += connector;
            ret += entry.replace(" ", connector);
        }
        return ret;
    }

    public static String getFileName(StringBuilder sb) {
        MessageDigest messageDigest = null;
        try {
            messageDigest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            LOGGER.error("Unable to find SHA-256 algorithm to generate file name.");
            e.printStackTrace();
        }
        byte[] bytesOfMessage = new byte[0];
        try {
            bytesOfMessage = sb.toString().getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            LOGGER.error("Unable to get UTF-8 content as bytes to generate file name.");
            e.printStackTrace();
        }
        byte[] hash = messageDigest.digest(bytesOfMessage);
        return Hex.encodeHexString(hash);
    }
}
