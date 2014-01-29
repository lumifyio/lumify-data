package com.altamiracorp.lumify.mapping.csv;

import com.altamiracorp.lumify.core.ingest.term.extraction.TermMention;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import java.util.List;

/**
 * A mapping from a particular CSV column to an Entity.
 */
@JsonInclude(Include.NON_EMPTY)
@JsonTypeInfo(include=As.PROPERTY, property="conceptLabelType", use=Id.NAME, defaultImpl=ConstantCsvEntityColumnMapping.class)
@JsonSubTypes({
    @Type(ConstantCsvEntityColumnMapping.class),
    @Type(ColumnLookupCsvEntityColumnMapping.class)
})
public interface CsvEntityColumnMapping extends Comparable<CsvEntityColumnMapping> {
    /**
     * The default value for required.
     */
    boolean DEFAULT_REQUIRED = false;

    /**
     * Get the 0-indexed column containing the sign of the Entity.
     * @return the column index containing the sign of the Entity
     */
    @JsonProperty("signColumn")
    int getColumnIndex();

    /**
     * <code>true</code> if this entity is required.
     * @return <code>true</code> if this entity is required
     */
    @JsonProperty("required")
    boolean isRequired();

    /**
     * Generate a TermMention, with all associated properties, from the fields
     * identified in a line of an incoming CSV document.
     * @param fields the CSV fields
     * @param offset the current document offset
     * @param processId the ID of the process reading this file
     * @return the generated TermMention
     */
    TermMention mapTerm(final List<String> fields, final int offset, final String processId);
}
