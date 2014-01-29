package com.altamiracorp.lumify.mapping.csv;

import static com.google.common.base.Preconditions.checkNotNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import java.util.List;

/**
 * An Entity mapping whose Ontological class (concept label) is dynamically
 * determined at runtime.
 */
@JsonTypeName("column")
public class ColumnLookupCsvEntityColumnMapping extends AbstractCsvEntityColumnMapping {
    /**
     * The field mapping used for concept resolution.
     */
    private final CsvPropertyColumnMapping<String> conceptLabelMapping;

    /**
     * Create a new CsvEntityColumnMapping.
     * @param index the column index
     * @param useExisting true to re-use an existing Term with the same sign
     * @param required is this Term required
     * @param props mappings for the properties of this Term
     * @param labelMapping the column mapping for concept label resolution
     */
    @JsonCreator
    @SuppressWarnings("unchecked")
    public ColumnLookupCsvEntityColumnMapping(@JsonProperty("signColumn") final int index,
            @JsonProperty(value="useExisting", required=false) final Boolean useExisting,
            @JsonProperty(value="properties", required=false) final List<CsvPropertyColumnMapping<?>> props,
            @JsonProperty(value="required", required=false) final Boolean required,
            @JsonProperty("conceptLabel") final CsvPropertyColumnMapping<String> labelMapping) {
        super(index, useExisting, props, required);
        checkNotNull(labelMapping, "Concept label mapping must be provided");
        this.conceptLabelMapping = labelMapping;
    }

    @JsonProperty("conceptLabel")
    public CsvPropertyColumnMapping<String> getConceptLabelMapping() {
        return conceptLabelMapping;
    }

    @Override
    protected String getConceptLabel(final List<String> fields) {
        return conceptLabelMapping.getPropertyValue(fields);
    }
}
