package com.altamiracorp.reddawn.model;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GeoLocation {
    private static Pattern POINT_REGEX = Pattern.compile("POINT\\((.+?),(.+?)\\)");

    public static Double getLatitude(String geoLocation) {
        if (geoLocation == null) {
            return null;
        }
        Matcher m = POINT_REGEX.matcher(geoLocation);
        if (!m.matches()) {
            throw new RuntimeException("Invalid point format: " + geoLocation);
        }
        return Double.parseDouble(m.group(1));
    }

    public static Double getLongitude(String geoLocation) {
        if (geoLocation == null) {
            return null;
        }
        Matcher m = POINT_REGEX.matcher(geoLocation);
        if (!m.matches()) {
            throw new RuntimeException("Invalid point format: " + geoLocation);
        }
        return Double.parseDouble(m.group(2));
    }
}
