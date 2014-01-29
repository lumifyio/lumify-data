package com.altamiracorp.lumify.mapping.csv;

import static com.google.common.base.Preconditions.*;

import com.altamiracorp.lumify.core.ingest.term.extraction.TermMention;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonTypeName;

/**
 * A mapping for a relationship between two Terms defined in
 * this CSV.
 */
@JsonPropertyOrder({ "label", "source", "target" })
@JsonTypeName("constant")
public class ConstantCsvRelationshipMapping extends AbstractCsvRelationshipMapping {
    /**
     * The relationship label.
     */
    private final String label;

    /**
     * Create a new CsvRelationshipMapping.
     * @param lbl the label for the mapping
     * @param srcId the mapId of the source Term (as defined in the mapping)
     * @param tgtId the mapId of the target Term (as defined in the mapping)
     */
    @JsonCreator
    public ConstantCsvRelationshipMapping(@JsonProperty("label") final String lbl,
            @JsonProperty("source") final String srcId,
            @JsonProperty("target") final String tgtId) {
        super(srcId, tgtId);
        checkNotNull(lbl, "label must be provided");
        checkArgument(!lbl.trim().isEmpty(), "label must be provided");
        this.label = lbl.trim();
    }

    @JsonProperty("label")
    public String getLabel() {
        return label;
    }

    @Override
    public String getLabel(final TermMention source, final TermMention target) {
        return getLabel();
    }
}
