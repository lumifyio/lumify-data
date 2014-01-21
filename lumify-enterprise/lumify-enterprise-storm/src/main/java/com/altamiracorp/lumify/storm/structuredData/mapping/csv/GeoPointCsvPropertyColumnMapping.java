/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.altamiracorp.lumify.storm.structuredData.mapping.csv;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.thinkaurelius.titan.core.attribute.Geoshape;
import java.util.List;

/**
 * This mapping uses two columns from a CSV to create a GeoPoint from
 * double-valued latitude and longitude properties.  If this property
 * is required, the getPropertyValue() method will generate an exception
 * if either the latitude or longitude columns are empty or invalid.
 */
@JsonTypeName("geopoint")
public class GeoPointCsvPropertyColumnMapping implements CsvPropertyColumnMapping<Geoshape> {
    /**
     * The name of the property.
     */
    private final String name;

    /**
     * The latitude value column index.
     */
    private final int latitudeColumnIndex;

    /**
     * The longitude value column index.
     */
    private final int longitudeColumnIndex;

    /**
     * Is this property required.
     */
    private final boolean required;

    /**
     * Create a new GeoPointCsvPropertyColumnMapping.
     * @param name the name of the property
     * @param latIdx the index of the latitude column
     * @param longIdx the index of the longitude column
     * @param reqd true if this property is required
     */
    public GeoPointCsvPropertyColumnMapping(@JsonProperty("name") final String name,
            @JsonProperty("latitudeColumnIndex") final int latIdx,
            @JsonProperty("longitudeColumnIndex") final int longIdx,
            @JsonProperty(value="required", required=false) final Boolean reqd) {
        this.name = name;
        this.latitudeColumnIndex = latIdx;
        this.longitudeColumnIndex = longIdx;
        this.required = reqd != null ? reqd : CsvPropertyColumnMapping.DEFAULT_REQUIRED;
    }

    @Override
    public final String getName() {
        return name;
    }

    public final int getLatitudeColumnIndex() {
        return latitudeColumnIndex;
    }

    public final int getLongitudeColumnIndex() {
        return longitudeColumnIndex;
    }

    @Override
    public final boolean isRequired() {
        return required;
    }

    @Override
    public Geoshape getPropertyValue(final List<String> fields) {
        Geoshape point;
        try {
            double latitude = Double.parseDouble(fields.get(latitudeColumnIndex));
            double longitude = Double.parseDouble(fields.get(longitudeColumnIndex));
            point = Geoshape.point(latitude, longitude);
        } catch (NumberFormatException nfe) {
            point = null;
        }
        if (required && point == null) {
            throw new IllegalArgumentException(String.format("%s [lat: %d, long: %d] is a required property.", name,
                    latitudeColumnIndex, longitudeColumnIndex));
        }
        return point;
    }
}
