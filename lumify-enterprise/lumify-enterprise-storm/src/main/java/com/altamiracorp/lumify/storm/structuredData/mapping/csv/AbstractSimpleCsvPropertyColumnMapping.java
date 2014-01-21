/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.altamiracorp.lumify.storm.structuredData.mapping.csv;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * Base class for simple CSV property mappings that map a single CSV column
 * to a property value.
 * @param <T> the type of value expected for this property
 */
public abstract class AbstractSimpleCsvPropertyColumnMapping<T> implements CsvPropertyColumnMapping<T> {
    /**
     * The column index for this property.
     */
    private final int columnIndex;

    /**
     * The name of this property.
     */
    private final String name;

    /**
     * Is this property required?
     */
    private final boolean required;

    /**
     * Create a new AbstractSimpleCsvPropertyColumnMapping for the given column index.
     * @param index the column index
     * @param nm the property name
     * @param reqd <code>true</code> if this column is required
     */
    protected AbstractSimpleCsvPropertyColumnMapping(final int index, final String nm, final Boolean reqd) {
        this.columnIndex = index;
        this.name = nm;
        this.required = reqd != null ? reqd : CsvPropertyColumnMapping.DEFAULT_REQUIRED;
    }

    /**
     * Get the column index.
     * @return the column index
     */
    @JsonProperty("column")
    public final int getColumnIndex() {
        return columnIndex;
    }

    @Override
    public final String getName() {
        return name;
    }

    @Override
    public final boolean isRequired() {
        return required;
    }

    @Override
    public final T getPropertyValue(final List<String> fields) {
        T value = fromString(fields.get(getColumnIndex()));
        if (required && value == null) {
            throw new IllegalArgumentException(String.format("%s is a required property.", name));
        }
        return value;
    }

    /**
     * Convert the String value found in the target CSV column to
     * the expected type of this property.
     * @param fieldValue the String value of the field
     * @return the converted value
     */
    protected abstract T fromString(final String fieldValue);

    /**
     * Returns <code>true</code> if the string is null or, when trimmed, the empty string.
     * @param str the string to check
     * @return true if the value is null or empty
     */
    protected final boolean isNullOrEmpty(final String str) {
        return str == null || str.trim().isEmpty();
    }
}
