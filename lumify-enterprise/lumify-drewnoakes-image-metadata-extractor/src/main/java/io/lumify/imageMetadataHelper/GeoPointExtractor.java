package io.lumify.imageMetadataHelper;

import com.drew.lang.GeoLocation;
import com.drew.metadata.Metadata;
import com.drew.metadata.MetadataException;
import com.drew.metadata.exif.GpsDirectory;
import io.lumify.core.util.LumifyLogger;
import io.lumify.core.util.LumifyLoggerFactory;
import org.securegraph.type.GeoPoint;

public class GeoPointExtractor {
    private static final LumifyLogger LOGGER = LumifyLoggerFactory.getLogger(GeoPointExtractor.class);

    public static GeoPoint getGeoPoint(Metadata metadata) {

        GeoPoint geoPoint = null;
        GpsDirectory gpsDir = metadata.getDirectory(GpsDirectory.class);
        if (gpsDir != null) {
            GeoLocation geoLocation = gpsDir.getGeoLocation();
            Double latitude = geoLocation.getLatitude();
            Double longitude = geoLocation.getLongitude();
            Double altitude = null;
            try {
                altitude = gpsDir.getDouble(GpsDirectory.TAG_GPS_ALTITUDE);
            } catch (MetadataException e) {
                //No code needed. Altitude is already null.
            }

            if (latitude != null && latitude != 0 && longitude != null && longitude != 0) {
                if (altitude != null && altitude != 0) {
                    geoPoint = new GeoPoint(latitude, longitude, altitude);
                    return geoPoint;
                } else {
                    geoPoint = new GeoPoint(latitude, longitude);
                    return geoPoint;
                }
            }
        }
        return null;
    }

    public static GeoPoint getGeoPointWithDirection(Metadata metadata) {
        GeoPoint originalPoint = getGeoPoint(metadata);

        GpsDirectory gpsDir = metadata.getDirectory(GpsDirectory.class);
        Double imageFacingDirection = getImageFacingDirection(metadata);
        if (imageFacingDirection != null) {
            String directionString = convertDegreeToDirection(imageFacingDirection);
            if (directionString != null) {
                String imageDirectionString = "Direction: " + directionString +
                        " (" + Math.round(imageFacingDirection) + "°)";
                GeoPoint geoPointWithDirection = new GeoPoint(originalPoint.getLatitude(),
                        originalPoint.getLongitude(),
                        imageDirectionString);
                return geoPointWithDirection;
            }
        }
        //Upon failure to get direction, return original GeoPoint.
        return originalPoint;
    }

    public static Double getImageFacingDirection(Metadata metadata) {
        GpsDirectory gpsDir = metadata.getDirectory(GpsDirectory.class);
        Double imageFacingDirection = null;
        if (gpsDir != null) {
            //TODO. Assumes true direction for IMG_DIRECTION. Can check TAG_GPS_IMG_DIRECTION_REF to be more specific.
            try {
                imageFacingDirection = gpsDir.getDouble(GpsDirectory.TAG_GPS_IMG_DIRECTION);
            } catch (MetadataException e) {
                LOGGER.debug("getDouble(TAG_GPS_IMAGE_DIRECTION) threw MetadataException when attempting to" +
                        "retrieve GPS Image Direction.");
            }
        }

        return imageFacingDirection;

    }

    private static String convertDegreeToDirection(double degree) {
        if (degree > 22.5 && degree <= 67.5) {
            return "NE";
        } else if (degree > 67.5 && degree <= 112.5) {
            return "E";
        } else if (degree > 112.5 && degree <= 157.5) {
            return "SE";
        } else if (degree > 157.5 && degree <= 202.5) {
            return "S";
        } else if (degree > 202.5 && degree <= 247.5) {
            return "SW";
        } else if (degree > 247.5 && degree <= 292.5) {
            return "W";
        } else if (degree > 292.5 && degree <= 337.5) {
            return "NW";
        } else if (degree > 337.5 && degree <= 360
                && degree >= 0 && degree <= 22.5) {
            return "N";
        } else {
            //because improper degree. Negative degree.
            return null;
        }
    }
}
