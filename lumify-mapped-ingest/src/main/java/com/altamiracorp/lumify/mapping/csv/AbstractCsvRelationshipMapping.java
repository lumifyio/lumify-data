package com.altamiracorp.lumify.mapping.csv;

import static com.google.common.base.Preconditions.*;

import com.altamiracorp.lumify.core.ingest.term.extraction.TermMention;
import com.altamiracorp.lumify.core.ingest.term.extraction.TermRelationship;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * A mapping for a relationship between two Terms defined in
 * this CSV.
 */
public abstract class AbstractCsvRelationshipMapping implements CsvRelationshipMapping {
    /**
     * The source term map ID.
     */
    private final String sourceTermId;

    /**
     * The target term map ID.
     */
    private final String targetTermId;

    /**
     * Create a new AbstractCsvRelationshipMapping.
     * @param srcId the mapId of the source Term (as defined in the mapping)
     * @param tgtId the mapId of the target Term (as defined in the mapping)
     */
    protected AbstractCsvRelationshipMapping(final String srcId, final String tgtId) {
        checkNotNull(srcId, "source must be provided");
        checkArgument(!srcId.trim().isEmpty(), "source must be provided");
        checkNotNull(tgtId, "target must be provided");
        checkArgument(!tgtId.trim().isEmpty(), "target must be provided");
        this.sourceTermId = srcId.trim();
        this.targetTermId = tgtId.trim();
    }

    @JsonProperty("source")
    @Override
    public final String getSourceTermId() {
        return sourceTermId;
    }

    @JsonProperty("target")
    @Override
    public final String getTargetTermId() {
        return targetTermId;
    }

    /**
     * Get the label for the relationship between the provided terms.
     * @param source the source term
     * @param target the target term
     * @return the relationship label or <code>null</code> if it cannot be determined
     */
    protected abstract String getLabel(final TermMention source, final TermMention target);

    @Override
    public final TermRelationship createRelationship(final TermMention source, final TermMention target) {
        String label = getLabel(source, target);
        TermRelationship relationship = null;
        if (source != null && target != null && label != null) {
            relationship = new TermRelationship(source, target, label);
        }
        return relationship;
    }
}
