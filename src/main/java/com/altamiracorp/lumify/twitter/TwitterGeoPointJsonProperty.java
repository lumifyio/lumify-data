/*
 * Copyright 2014 Altamira Corporation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.altamiracorp.lumify.twitter;

import com.altamiracorp.lumify.core.json.JsonProperty;
import com.thinkaurelius.titan.core.attribute.Geoshape;
import org.json.JSONArray;

/**
 * A Twitter JSON property whose value is a Geo-coordinate array.
 */
public class TwitterGeoPointJsonProperty extends JsonProperty<Geoshape, JSONArray> {
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
    protected Geoshape fromJSON(final JSONArray jsonValue) {
        double latitude = jsonValue.optDouble(LATITUDE, NO_COORDINATE);
        double longitude = jsonValue.optDouble(LONGITUDE, NO_COORDINATE);
        return latitude != NO_COORDINATE && longitude != NO_COORDINATE ? Geoshape.point(latitude, longitude) : null;
    }

    @Override
    protected JSONArray toJSON(final Geoshape value) {
        JSONArray array = null;
        if (value != null) {
            array = new JSONArray();
            array.put(LONGITUDE, (double) value.getPoint().getLongitude());
            array.put(LATITUDE, (double) value.getPoint().getLatitude());
        }
        return array;
    }
}
