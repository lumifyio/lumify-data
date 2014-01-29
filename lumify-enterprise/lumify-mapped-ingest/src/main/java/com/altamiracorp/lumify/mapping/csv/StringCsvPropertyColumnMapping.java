/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.altamiracorp.lumify.mapping.csv;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;

/**
 * The mapping for String properties.
 */
@JsonTypeName("string")
public class StringCsvPropertyColumnMapping extends AbstractSimpleCsvPropertyColumnMapping<String> {
    /**
     * Create a new StringCsvPropertyColumnMapping.
     * @param index the column index
     * @param name the property name
     * @param required <code>true</code> if the property is required
     */
    @JsonCreator
    public StringCsvPropertyColumnMapping(@JsonProperty("column") final int index,
            @JsonProperty("name") final String name,
            @JsonProperty(value="required", required=false) final Boolean required) {
        super(index, name, required);
    }

    @Override
    protected String fromString(final String fieldValue) {
        return isNullOrEmpty(fieldValue) ? null : fieldValue;
    }
}
