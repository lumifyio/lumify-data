/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 *//*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 *//*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 *//*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.altamiracorp.lumify.storm.structuredData.mapping.csv;

import com.altamiracorp.lumify.core.ingest.term.extraction.TermMention;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * The mapping for a CSV Column that represents a Lumify Term.  The value of
 * the column will be used as the sign (value) of the Term.
 */
@JsonInclude(Include.NON_EMPTY)
public class CsvTermColumnMapping implements Comparable<CsvTermColumnMapping> {
    /**
     * The default value for required.
     */
    public static final boolean DEFAULT_REQUIRED = false;

    /**
     * The default value for useExisting.
     */
    public static final boolean DEFAULT_USE_EXISTING = false;

    /**
     * The column index for this mapping.
     */
    private final int columnIndex;

    /**
     * The map key for this Term, used to uniquely identify it
     * within a relationship definition.
     */
    private final String mapId;

    /**
     * The ontological concept label for this term.
     */
    private final String conceptLabel;

    /**
     * If true, an existing Term with the same sign will be re-used instead
     * of creating a new Term object.  If false, a new Term object will
     * always be created.
     */
    private final boolean useExisting;

    /**
     * If true, this Term is required and, if the target column has no value
     * (including whitespace only values), an exception will be thrown when parsing.
     */
    private final boolean required;

    /**
     * Mappings for the properties of this Term.
     */
    private final List<CsvPropertyColumnMapping<?>> properties;

    /**
     * Create a new CsvTermColumnMapping.
     * @param index the column index
     * @param mId the unique key used to identify this Term in a relationship definition
     * @param label the ontological concept label for the Term
     * @param useExistingIn true to re-use an existing Term with the same sign
     * @param reqd is this Term required
     * @param props mappings for the properties of this Term
     */
    @JsonCreator
    @SuppressWarnings("unchecked")
    public CsvTermColumnMapping(@JsonProperty("column") final int index,
            @JsonProperty("mapId") final String mId,
            @JsonProperty("conceptLabel") final String label,
            @JsonProperty(value="useExisting", required=false) final Boolean useExistingIn,
            @JsonProperty(value="properties", required=false) final List<CsvPropertyColumnMapping<?>> props,
            @JsonProperty(value="required", required=false) final Boolean reqd) {
        this.columnIndex = index;
        this.mapId = mId;
        this.conceptLabel = label;
        this.useExisting = useExistingIn != null ? useExistingIn : CsvTermColumnMapping.DEFAULT_USE_EXISTING;
        this.required = reqd != null ? reqd : CsvTermColumnMapping.DEFAULT_REQUIRED;
        this.properties =
                props != null ? Collections.unmodifiableList(new ArrayList<CsvPropertyColumnMapping<?>>(props)) : Collections.EMPTY_LIST;
    }

    @JsonProperty("column")
    public int getColumnIndex() {
        return columnIndex;
    }

    @JsonProperty("mapId")
    public String getMapId() {
        return mapId;
    }

    @JsonProperty("conceptLabel")
    public String getConceptLabel() {
        return conceptLabel;
    }

    @JsonProperty("useExisting")
    public boolean isUseExisting() {
        return useExisting;
    }

    @JsonProperty("required")
    public boolean isRequired() {
        return required;
    }

    @JsonProperty("properties")
    public List<CsvPropertyColumnMapping<?>> getProperties() {
        return properties;
    }

    /**
     * Generate a TermMention, with all associated properties, from the fields
     * identified in a line of an incoming CSV document.
     * @param fields the CSV fields
     * @param offset the current document offset
     * @param processId the ID of the process reading this file
     * @return the generated TermMention
     */
    public TermMention mapTerm(final List<String> fields, final int offset, final String processId) {
        String sign = fields.get(columnIndex);
        TermMention mention;
        if (sign == null || sign.trim().isEmpty()) {
            if (required) {
                throw new IllegalArgumentException(String.format("Term [%s] in CSV column %d is required.", mapId, columnIndex));
            } else {
                mention = null;
            }
        } else {
            TermMention.Builder builder = new TermMention.Builder()
                    .start(offset)
                    .end(offset + sign.length())
                    .sign(sign)
                    .ontologyClassUri(conceptLabel)
                    .useExisting(useExisting)
                    .resolved(true)
                    .process(processId);
            for (CsvPropertyColumnMapping<?> prop : properties) {
                builder.setProperty(prop.getName(), prop.getPropertyValue(fields));
            }
            mention = builder.build();
        }
        return mention;
    }

    @Override
    public int compareTo(final CsvTermColumnMapping o) {
        return this.columnIndex - o.columnIndex;
    }
}
