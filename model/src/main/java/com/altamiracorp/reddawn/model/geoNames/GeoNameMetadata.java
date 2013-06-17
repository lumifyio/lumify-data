package com.altamiracorp.reddawn.model.geoNames;

import com.altamiracorp.reddawn.model.ColumnFamily;
import com.altamiracorp.reddawn.model.Value;

public class GeoNameMetadata extends ColumnFamily {
    public static final String NAME = "Metadata";
    public static final String NAME_COLUMN = "name";
    public static final String LATITUDE = "latitude";
    public static final String LONGITUDE = "longitude";
    public static final String POPULATION = "population";

    public GeoNameMetadata() {
        super(NAME);
    }

    public String getName() {
        return Value.toString(get(NAME_COLUMN));
    }

    public GeoNameMetadata setName(String name) {
        set(NAME_COLUMN, name);
        return this;
    }

    public Double getLatitude() {
        return Value.toDouble(get(LATITUDE));
    }

    public GeoNameMetadata setLatitude(double latitude) {
        set(LATITUDE, latitude);
        return this;
    }

    public Double getLongitude() {
        return Value.toDouble(get(LONGITUDE));
    }

    public GeoNameMetadata setLongitude(Double longitude) {
        set(LONGITUDE, longitude);
        return this;
    }

    public Long getPopulation() {
        return Value.toLong(get(POPULATION));
    }

    public GeoNameMetadata setPopulation(Long population) {
        set(POPULATION, population);
        return this;
    }
}
