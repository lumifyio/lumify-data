package com.altamiracorp.lumify.mapping.column;

import static com.google.common.base.Preconditions.*;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import java.util.List;
import java.util.Map;

/**
 * A ColumnEntityMapping with a configured Concept URI.
 */
@JsonTypeName("constant")
public class ConstantConceptColumnEntityMapping extends AbstractColumnEntityMapping {
    /**
     * The configured concept URI.
     */
    private final String conceptURI;

    /**
     * Create a new ConstantConceptColumnEntityMapping.
     * @param signCol the ColumnValue providing the sign of this entity
     * @param concept the concept URI for this entity
     * @param props the properties of this entity
     * @param useExisting should existing entities be reused? null for default
     * @param required is this entity required? null for default
     */
    public ConstantConceptColumnEntityMapping(@JsonProperty("signColumn") final ColumnValue<String> signCol,
            @JsonProperty("conceptURI") final String concept,
            @JsonProperty(value="properties", required=false) final Map<String, ColumnValue<?>> props,
            @JsonProperty(value="useExisting", required=false) final Boolean useExisting,
            @JsonProperty(value="required", required=false) final Boolean required) {
        super(signCol, props, useExisting, required);
        checkNotNull(concept, "Concept URI must be provided");
        checkArgument(!concept.trim().isEmpty(), "Concept URI must be provided");
        this.conceptURI = concept.trim();
    }

    @Override
    protected String getConceptURI(final List<String> row) {
        return conceptURI;
    }

    @JsonProperty("conceptURI")
    public final String getConceptURI() {
        return conceptURI;
    }
}