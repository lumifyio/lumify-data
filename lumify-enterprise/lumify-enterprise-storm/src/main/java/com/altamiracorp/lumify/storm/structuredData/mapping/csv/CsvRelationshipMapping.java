/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.altamiracorp.lumify.storm.structuredData.mapping.csv;

import static com.google.common.base.Preconditions.*;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * A mapping for a relationship between two Terms defined in
 * this CSV.
 */
@JsonPropertyOrder({ "label", "source", "target" })
public class CsvRelationshipMapping {
    /**
     * The relationship label.
     */
    private final String label;

    /**
     * The source term map ID.
     */
    private final String sourceTermId;

    /**
     * The target term map ID.
     */
    private final String targetTermId;

    /**
     * Create a new CsvRelationshipMapping.
     * @param lbl the label for the mapping
     * @param srcId the mapId of the source Term (as defined in the mapping)
     * @param tgtId the mapId of the target Term (as defined in the mapping)
     */
    @JsonCreator
    public CsvRelationshipMapping(@JsonProperty("label") final String lbl,
            @JsonProperty("source") final String srcId,
            @JsonProperty("target") final String tgtId) {
        checkNotNull(lbl, "label must be provided");
        checkArgument(!lbl.trim().isEmpty(), "label must be provided");
        checkNotNull(srcId, "source must be provided");
        checkArgument(!srcId.trim().isEmpty(), "source must be provided");
        checkNotNull(tgtId, "target must be provided");
        checkArgument(!tgtId.trim().isEmpty(), "target must be provided");
        this.label = lbl.trim();
        this.sourceTermId = srcId.trim();
        this.targetTermId = tgtId.trim();
    }

    @JsonProperty("label")
    public String getLabel() {
        return label;
    }

    @JsonProperty("source")
    public String getSourceTermId() {
        return sourceTermId;
    }

    @JsonProperty("target")
    public String getTargetTermId() {
        return targetTermId;
    }
}
