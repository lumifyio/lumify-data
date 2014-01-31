package com.altamiracorp.lumify.mapping.csv;

import com.altamiracorp.lumify.core.ingest.term.extraction.TermMention;
import com.altamiracorp.lumify.core.ingest.term.extraction.TermRelationship;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;

/**
 * A mapping for a relationship between two Terms defined in
 * a CSV.
 */
@JsonTypeInfo(include=As.PROPERTY, property="labelType", use=Id.NAME, defaultImpl=ConstantCsvRelationshipMapping.class)
@JsonSubTypes({
    @Type(ConstantCsvRelationshipMapping.class),
    @Type(EntityConceptMappedCsvRelationshipMapping.class)
})
public interface CsvRelationshipMapping {
    /**
     * Get the ID configured in the mapping for the source entity of this relationship
     * @return the source entity ID
     */
    @JsonProperty(value = "source")
    String getSourceTermId();

    /**
     * Get the ID configured in the mapping for the target entity of this relationship
     * @return the target entity ID
     */
    @JsonProperty(value = "target")
    String getTargetTermId();

    /**
     * Create a relationship between the provided TermMentions, returning <code>null</code>
     * if the relationship could not be created.
     * @param source the source term
     * @param target the target term
     * @return the created relationship between the two terms
     */
    TermRelationship createRelationship(final TermMention source, final TermMention target);
}
