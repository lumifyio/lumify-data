package com.altamiracorp.lumify.mapping.csv;

import static com.google.common.base.Preconditions.*;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import java.util.List;

/**
 * An entity column mapping that uses a configured ontological concept type.
 */
@JsonTypeName("constant")
public class ConstantCsvEntityColumnMapping extends AbstractCsvEntityColumnMapping {
    /**
     * The ontological concept label for this term.
     */
    private final String conceptLabel;

    /**
     * Create a new CsvEntityColumnMapping.
     * @param index the column index
     * @param useExisting true to re-use an existing Term with the same sign
     * @param required is this Term required
     * @param props mappings for the properties of this Term
     * @param label the ontological concept label for the Term
     */
    @JsonCreator
    @SuppressWarnings("unchecked")
    public ConstantCsvEntityColumnMapping(@JsonProperty("signColumn") final int index,
            @JsonProperty(value="useExisting", required=false) final Boolean useExisting,
            @JsonProperty(value="properties", required=false) final List<CsvPropertyColumnMapping<?>> props,
            @JsonProperty(value="required", required=false) final Boolean required,
            @JsonProperty("conceptLabel") final String label) {
        super(index, useExisting, props, required);
        checkNotNull(label, "Concept label must be provided");
        checkArgument(!label.trim().isEmpty(), "Concept label must be provided");
        this.conceptLabel = label.trim();
    }

    @JsonProperty("conceptLabel")
    public final String getConceptLabel() {
        return conceptLabel;
    }

    @Override
    protected String getConceptLabel(final List<String> fields) {
        return getConceptLabel();
    }
}
