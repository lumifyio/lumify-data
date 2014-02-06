/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.altamiracorp.lumify.mapping.column;

import static com.google.common.base.Preconditions.checkNotNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import java.util.List;
import java.util.Map;

/**
 * An entity mapping that gets its Concept URI from the values in
 * an input column.
 */
@JsonTypeName("columnLookup")
public class ConceptLookupColumnEntityMapping extends AbstractColumnEntityMapping {
    /**
     * The ColumnValue resolving to the Concept URI.
     */
    private final ColumnValue<String> conceptColumn;

    /**
     * Create a new ConstantConceptColumnEntityMapping.
     * @param idCol the ColumnValue providing the ID of this entity; null for auto-generated ID
     * @param signCol the ColumnValue providing the sign of this entity
     * @param concept the ColumnValue providing the concept URI for this entity
     * @param props the properties of this entity
     * @param useExisting should existing entities be reused? null for default
     * @param required is this entity required? null for default
     */
    @JsonCreator
    public ConceptLookupColumnEntityMapping(@JsonProperty("idColumn") final ColumnValue<?> idCol,
            @JsonProperty("signColumn") final ColumnValue<String> signCol,
            @JsonProperty("conceptColumn") final ColumnValue<String> concept,
            @JsonProperty(value="properties", required=false) final Map<String, ColumnValue<?>> props,
            @JsonProperty(value="useExisting", required=false) final Boolean useExisting,
            @JsonProperty(value="required", required=false) final Boolean required) {
        super(idCol, signCol, props, useExisting, required);
        checkNotNull(concept, "Concept column must be provided");
        this.conceptColumn = concept;
    }

    @JsonProperty("conceptColumn")
    public final ColumnValue<String> getConceptColumn() {
        return conceptColumn;
    }

    @Override
    protected String getConceptURI(final List<String> row) {
        return conceptColumn.getValue(row);
    }
}
