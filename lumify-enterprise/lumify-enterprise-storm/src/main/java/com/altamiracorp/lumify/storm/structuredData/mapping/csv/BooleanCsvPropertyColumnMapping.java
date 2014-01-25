/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.altamiracorp.lumify.storm.structuredData.mapping.csv;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;

/**
 * The mapping for Boolean properties.
 */
@JsonTypeName("boolean")
public class BooleanCsvPropertyColumnMapping extends AbstractSimpleCsvPropertyColumnMapping<Boolean> {
    /**
     * Create a new BooleanCsvPropertyColumnMapping.
     * @param index the column index
     * @param name the property name
     * @param required <code>true</code> if the property is required
     */
    @JsonCreator
    public BooleanCsvPropertyColumnMapping(@JsonProperty("column") final int index,
            @JsonProperty("name") final String name,
            @JsonProperty(value="required", required=false) final Boolean required) {
        super(index, name, required);
    }

    @Override
    protected Boolean fromString(final String fieldValue) {
        return isNullOrEmpty(fieldValue) ? null : Boolean.valueOf(fieldValue);
    }
}
