/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 *//*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.altamiracorp.lumify.mapping.csv;

import static com.google.common.base.Preconditions.*;

import com.altamiracorp.securegraph.type.GeoCircle;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import java.util.List;

/**
 * This mapping uses three columns from a CSV to create a GeoCircle from
 * double-valued latitude, longitude and radius properties.  If this property
 * is required, the getPropertyValue() method will generate an exception
 * if either the latitude, longitude or radius columns are empty or invalid.
 */
@JsonTypeName("geocircle")
public class GeoCircleCsvPropertyColumnMapping implements CsvPropertyColumnMapping<GeoCircle> {
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
     * The radius value column index.
     */
    private final int radiusColumnIndex;

    /**
     * Is this property required.
     */
    private final boolean required;

    /**
     * Create a new GeoPointCsvPropertyColumnMapping.
     * @param name the name of the property
     * @param latIdx the index of the latitude column
     * @param longIdx the index of the longitude column
     * @param radIdx the index of the radius column
     * @param reqd true if this property is required
     */
    public GeoCircleCsvPropertyColumnMapping(@JsonProperty("name") final String name,
            @JsonProperty("latitudeColumn") final int latIdx,
            @JsonProperty("longitudeColumn") final int longIdx,
            @JsonProperty("radiusColumn") final int radIdx,
            @JsonProperty(value="required", required=false) final Boolean reqd) {
        checkNotNull(name, "name must be provided");
        checkArgument(!name.trim().isEmpty(), "name must be provided");
        checkArgument(latIdx >= 0, "latitudeColumn must be >= 0");
        checkArgument(longIdx >= 0, "longitudeColumn must be >= 0");
        checkArgument(radIdx >= 0, "radiusColumn must be >= 0");
        this.name = name.trim();
        this.latitudeColumnIndex = latIdx;
        this.longitudeColumnIndex = longIdx;
        this.radiusColumnIndex = radIdx;
        this.required = reqd != null ? reqd : CsvPropertyColumnMapping.DEFAULT_REQUIRED;
    }

    @Override
    public final String getName() {
        return name;
    }

    @JsonProperty("latitudeColumn")
    public final int getLatitudeColumnIndex() {
        return latitudeColumnIndex;
    }

    @JsonProperty("longitudeColumn")
    public final int getLongitudeColumnIndex() {
        return longitudeColumnIndex;
    }

    @JsonProperty("radiusColumn")
    public final int getRadiusColumnIndex() {
        return radiusColumnIndex;
    }

    @Override
    public final boolean isRequired() {
        return required;
    }

    @Override
    public GeoCircle getPropertyValue(final List<String> fields) {
        GeoCircle circle;
        try {
            double latitude = Double.parseDouble(fields.get(latitudeColumnIndex));
            double longitude = Double.parseDouble(fields.get(longitudeColumnIndex));
            double radius = Double.parseDouble(fields.get(radiusColumnIndex));
            circle = new GeoCircle(latitude, longitude, radius);
        } catch (NumberFormatException nfe) {
            circle = null;
        } catch (NullPointerException npe) {
            circle = null;
        }
        if (required && circle == null) {
            throw new IllegalArgumentException(String.format("%s [lat: %d, long: %d, rad: %d] is a required property.", name,
                    latitudeColumnIndex, longitudeColumnIndex, radiusColumnIndex));
        }
        return circle;
    }
}
