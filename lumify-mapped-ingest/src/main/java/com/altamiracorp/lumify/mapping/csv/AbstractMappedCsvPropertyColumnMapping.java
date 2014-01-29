/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.altamiracorp.lumify.mapping.csv;

import static com.google.common.base.Preconditions.*;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * A column whose input values are directly mapped to a set of output values.
 */
public abstract class AbstractMappedCsvPropertyColumnMapping<T> extends AbstractSimpleCsvPropertyColumnMapping<T> {
    /**
     * The value map.
     */
    private final Map<String, T> valueMap;

    /**
     * The default value.
     */
    private final T defaultValue;

    /**
     * Create a new AbstractMappedCsvPropertyColumnMapping.
     * @param index the column index
     * @param name the property name
     * @param reqd <code>true</code> if the property is required
     * @param valMap the map of input to output values
     * @param dfltVal the default value if the input is not found in the mapping
     */
    protected AbstractMappedCsvPropertyColumnMapping(final int index, final String name, final Boolean reqd,
            final Map<String, T> valMap, final T dfltVal) {
        super(index, name, reqd);
        checkNotNull(valMap, "Value map must be provided.");
        this.valueMap = Collections.unmodifiableMap(new HashMap<String, T>(valMap));
        this.defaultValue = dfltVal;
    }

    @Override
    protected final T fromString(final String fieldValue) {
        T value = null;
        if (!isNullOrEmpty(fieldValue)) {
            value = valueMap.get(fieldValue.trim());
        }
        return value != null ? value : defaultValue;
    }

    @JsonProperty("valueMap")
    public Map<String, T> getValueMap() {
        return valueMap;
    }

    @JsonProperty("defaultValue")
    public T getDefaultValue() {
        return defaultValue;
    }
}
