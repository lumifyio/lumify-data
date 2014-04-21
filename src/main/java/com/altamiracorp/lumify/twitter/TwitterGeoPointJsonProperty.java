package com.altamiracorp.lumify.twitter;

import com.altamiracorp.lumify.twitter.json.JsonProperty;
import com.altamiracorp.securegraph.type.GeoPoint;
import org.json.JSONArray;

/**
 * A Twitter JSON property whose value is a Geo-coordinate array.
 */
public class TwitterGeoPointJsonProperty extends JsonProperty<GeoPoint, JSONArray> {
    /**
     * The latitude index.
     */
    private static final int LATITUDE = 1;
    
    /**
     * The longitude index.
     */
    private static final int LONGITUDE = 0;
    
    /**
     * The default value to indicate latitude or longitude has not been set or cannot be parsed.
     */
    private static final double NO_COORDINATE = -999.0d;
    
    /**
     * Create a new TwitterGeoPointJsonProperty.
     * @param key the property key
     */
    public TwitterGeoPointJsonProperty(final String key) {
        super(key, JsonType.ARRAY);
    }

    @Override
    protected GeoPoint fromJSON(final JSONArray jsonValue) {
        double latitude = jsonValue.optDouble(LATITUDE, NO_COORDINATE);
        double longitude = jsonValue.optDouble(LONGITUDE, NO_COORDINATE);
        return latitude != NO_COORDINATE && longitude != NO_COORDINATE ? new GeoPoint(latitude, longitude) : null;
    }

    @Override
    protected JSONArray toJSON(final GeoPoint value) {
        JSONArray array = null;
        if (value != null) {
            array = new JSONArray();
            array.put(LONGITUDE, (double) value.getLongitude());
            array.put(LATITUDE, (double) value.getLatitude());
        }
        return array;
    }
}
