/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.altamiracorp.lumify.storm.structuredData.mapping.csv;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import java.util.List;

/**
 * The mapping definition for a property of a Term in a CSV.
 * @param <T> the type of property value returned by this mapping
 */
@JsonTypeInfo(include=As.PROPERTY, property="dataType", use=Id.NAME)
@JsonSubTypes({
    @Type(BooleanCsvPropertyColumnMapping.class),
    @Type(DateCsvPropertyColumnMapping.class),
    @Type(DoubleCsvPropertyColumnMapping.class),
    @Type(GeoPointCsvPropertyColumnMapping.class),
    @Type(GeoCircleCsvPropertyColumnMapping.class),
    @Type(IntegerCsvPropertyColumnMapping.class),
    @Type(LongCsvPropertyColumnMapping.class),
    @Type(StringCsvPropertyColumnMapping.class)
})
@JsonInclude(Include.NON_EMPTY)
@JsonPropertyOrder({ "dataType" })
public interface CsvPropertyColumnMapping<T> {
    /**
     * The default required state for mapped CSV properties.
     */
    boolean DEFAULT_REQUIRED = false;

    /**
     * Get the name of this property.
     * @return the name of the property as it will be stored in the Lumify system
     */
    @JsonProperty("name")
    String getName();

    /**
     * Is this field required?  If this is true, getPropertyValue() should
     * throw an exception if an invalid value or no value is provided
     * in the CSV.
     * @return true if this field is required
     */
    @JsonProperty("required")
    boolean isRequired();

    /**
     * Get the mapped property value from the current set of CSV fields.  If
     * this field is required, getPropertyValue() should throw an Exception
     * if a valid value cannot be resolved; otherwise it should return <code>null</code>.
     * @param fields the CSV fields
     * @return the mapped property value
     */
    @JsonIgnore
    T getPropertyValue(final List<String> fields);
}
