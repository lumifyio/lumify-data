/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.altamiracorp.lumify.storm.structuredData.mapping.csv;

import static com.google.common.base.Preconditions.*;

import com.altamiracorp.securegraph.type.GeoPoint;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import java.util.List;

/**
 * This mapping uses two columns from a CSV to create a GeoPoint from
 * double-valued latitude, longitude and optional altitude properties.
 * If this property is required, the getPropertyValue() method will
 * generate an exception if either the latitude or longitude columns
 * are empty or invalid.  If altitude is configured, it also must
 * be a valid value when required.
 */
@JsonTypeName("geopoint")
public class GeoPointCsvPropertyColumnMapping implements CsvPropertyColumnMapping<GeoPoint> {
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
     * The altitude value column index.
     */
    private final Integer altitudeColumnIndex;

    /**
     * Is this property required.
     */
    private final boolean required;

    /**
     * Create a new GeoPointCsvPropertyColumnMapping.
     * @param name the name of the property
     * @param latIdx the index of the latitude column
     * @param longIdx the index of the longitude column
     * @param altIdx the index of the optional altitude column
     * @param reqd true if this property is required
     */
    public GeoPointCsvPropertyColumnMapping(@JsonProperty("name") final String name,
            @JsonProperty("latitudeColumn") final int latIdx,
            @JsonProperty("longitudeColumn") final int longIdx,
            @JsonProperty(value="altitudeColumn", required=false) final Integer altIdx,
            @JsonProperty(value="required", required=false) final Boolean reqd) {
        checkNotNull(name, "name must be provided");
        checkArgument(!name.trim().isEmpty(), "name must be provided");
        checkArgument(latIdx >= 0, "latitudeColumn must be >= 0");
        checkArgument(longIdx >= 0, "longitudeColumn must be >= 0");
        checkArgument(altIdx == null || altIdx >= 0, "altitudeColumn must be >= 0 if provided");
        this.name = name.trim();
        this.latitudeColumnIndex = latIdx;
        this.longitudeColumnIndex = longIdx;
        this.altitudeColumnIndex = altIdx;
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

    @JsonProperty("altitudeColumn")
    public final Integer getAltitudeColumnIndex() {
        return altitudeColumnIndex;
    }

    @Override
    public final boolean isRequired() {
        return required;
    }

    @Override
    public GeoPoint getPropertyValue(final List<String> fields) {
        GeoPoint point;
        try {
            double latitude = Double.parseDouble(fields.get(latitudeColumnIndex));
            double longitude = Double.parseDouble(fields.get(longitudeColumnIndex));
            Double altitude = altitudeColumnIndex != null ? Double.parseDouble(fields.get(altitudeColumnIndex)) : null;
            point = new GeoPoint(latitude, longitude, altitude);
        } catch (NumberFormatException nfe) {
            point = null;
        } catch (NullPointerException npe) {
            point = null;
        }
        if (required && point == null) {
            throw new IllegalArgumentException(String.format("%s [lat: %d, long: %d, alt: %d] is a required property.", name,
                    latitudeColumnIndex, longitudeColumnIndex, altitudeColumnIndex));
        }
        return point;
    }
}
