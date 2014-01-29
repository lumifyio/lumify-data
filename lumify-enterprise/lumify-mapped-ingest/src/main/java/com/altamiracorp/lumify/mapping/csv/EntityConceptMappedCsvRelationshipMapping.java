package com.altamiracorp.lumify.mapping.csv;

import static com.google.common.base.Preconditions.*;

import com.altamiracorp.lumify.core.ingest.term.extraction.TermMention;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonTypeName;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * This relationship mapping looks up the appropriate label based on
 * the concept labels of the source and target entities.
 */
@JsonInclude(Include.NON_EMPTY)
@JsonTypeName("entityConcept")
@JsonPropertyOrder({ "mode", "source", "target", "label", "separator" })
public class EntityConceptMappedCsvRelationshipMapping extends AbstractCsvRelationshipMapping {
    /**
     * The mapping mode.
     */
    public static enum Mode {
        /**
         * Derive relationships using only the source entity concept.
         */
        SOURCE,
        /**
         * Derive relationships using only the target entity concept.
         */
        TARGET,
        /**
         * Derive relationships using both the source and target entity concepts.
         */
        ALL;
    }

    /**
     * The default separator string: &quot;::&quot;
     */
    public static final String DEFAULT_SEPARATOR = "::";

    /**
     * The mapping mode for this relationship mapping.
     */
    private final Mode mode;

    /**
     * The map of formatted entity concept labels to relationship labels.  The keys
     * of this map should be the expected source and/or target entity concepts.  If
     * both source and target entities are used, their concepts should be separated
     * by the configured separator string.  (e.g. sourceConcept::targetConcept).
     */
    private final Map<String, String> labelMap;

    /**
     * The separator string when using both source and target entity concepts to
     * map relationship labels.
     */
    private final String separator;

    /**
     * Create a new EntityConceptMappedCsvRelationshipMapping.
     * @param sourceId the mapId of the source Term (as defined in the mapping)
     * @param targetId the mapId of the target Term (as defined in the mapping)
     * @param mapMode the mapping mode
     * @param lblMap the map of entity concept labels to relationship labels
     * @param sep the separator used between the source and target entity concepts when mode is ALL
     */
    @JsonCreator
    public EntityConceptMappedCsvRelationshipMapping(@JsonProperty("source") final String sourceId,
            @JsonProperty("target") final String targetId,
            @JsonProperty("mode") final Mode mapMode,
            @JsonProperty("label") final Map<String, String> lblMap,
            @JsonProperty(value="separator", required=false) final String sep) {
        super(sourceId, targetId);
        checkNotNull(mapMode, "Mapping mode must be provided.");
        checkNotNull(lblMap, "Label map must be provided.");
        checkArgument(!lblMap.isEmpty(), "Label map must contain at least one entry.");
        checkArgument(sep == null || !sep.trim().isEmpty(), "Separator must contain at least one non-whitespace character if provided.");
        this.mode = mapMode;
        this.labelMap = Collections.unmodifiableMap(new HashMap<String, String>(lblMap));
        this.separator = sep != null ? sep.trim() : DEFAULT_SEPARATOR;
        // if in ALL mode, verify that map keys match separator pattern .+SEPARATOR.+
        if (this.mode == Mode.ALL) {
            String patternStr = String.format(".+%s.+", this.separator);
            Pattern keyPattern = Pattern.compile(patternStr);
            for (String key : this.labelMap.keySet()) {
                checkArgument(keyPattern.matcher(key).matches(),
                        String.format("Invalid Map Key [%s] must match the pattern: %s", key, patternStr));
            }
        }
    }

    @JsonProperty("mode")
    public final Mode getMode() {
        return mode;
    }

    @JsonProperty("label")
    public final Map<String, String> getLabelMap() {
        return labelMap;
    }

    @JsonProperty("separator")
    public final String getSeparator() {
        return separator;
    }

    @Override
    protected String getLabel(TermMention source, TermMention target) {
        String lookupKey = null;
        switch (mode) {
            case SOURCE:
                if (source != null) {
                    lookupKey = source.getOntologyClassUri();
                }
                break;
            case TARGET:
                if (target != null) {
                    lookupKey = target.getOntologyClassUri();
                }
                break;
            case ALL:
                if (source != null && target != null) {
                    lookupKey = String.format("%s%s%s", source.getOntologyClassUri(), separator, target.getOntologyClassUri());
                }
                break;
        }
        return lookupKey != null ? labelMap.get(lookupKey) : null;
    }
}
