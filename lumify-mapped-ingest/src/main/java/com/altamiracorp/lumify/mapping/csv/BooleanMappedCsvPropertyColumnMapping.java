/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.altamiracorp.lumify.mapping.csv;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import java.util.Map;

/**
 * A column whose input values are mapped to Boolean values.
 */
@JsonTypeName("mappedBoolean")
public class BooleanMappedCsvPropertyColumnMapping extends AbstractMappedCsvPropertyColumnMapping<Boolean> {
    /**
     * Create a new BooleanMappedCsvPropertyColumnMapping.
     * @param index the column index
     * @param name the property name
     * @param reqd is this property required
     * @param valueMap the value map
     * @param defaultValue the default value to return when the value is not contained in the map
     */
    @JsonCreator
    public BooleanMappedCsvPropertyColumnMapping(@JsonProperty("column") final int index,
            @JsonProperty("name") final String name,
            @JsonProperty(value="required", required=false) final Boolean reqd,
            @JsonProperty("valueMap") final Map<String, Boolean> valueMap,
            @JsonProperty(value="defaultValue", required=false) final Boolean defaultValue) {
        super(index, name, reqd, valueMap, defaultValue);
    }
}
