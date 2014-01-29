package com.altamiracorp.lumify.mapping.csv;

import static com.google.common.base.Preconditions.*;

import com.altamiracorp.lumify.core.ingest.term.extraction.TermMention;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * The mapping for a CSV Column that represents a Lumify Term.  The value of
 * the column will be used as the sign (value) of the Term.
 */
public abstract class AbstractCsvEntityColumnMapping implements CsvEntityColumnMapping {
    /**
     * The default value for useExisting.
     */
    public static final boolean DEFAULT_USE_EXISTING = false;

    /**
     * The column index for this mapping.
     */
    private final int columnIndex;

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
     * Create a new CsvEntityColumnMapping.
     * @param index the column index
     * @param useExistingIn true to re-use an existing Term with the same sign
     * @param reqd is this Term required
     * @param props mappings for the properties of this Term
     */
    @SuppressWarnings("unchecked")
    protected AbstractCsvEntityColumnMapping(final int index, final Boolean useExistingIn, final List<CsvPropertyColumnMapping<?>> props,
            final Boolean reqd) {
        checkArgument(index >= 0, "Column index must be >= 0");

        this.columnIndex = index;
        this.useExisting = useExistingIn != null ? useExistingIn : AbstractCsvEntityColumnMapping.DEFAULT_USE_EXISTING;
        this.required = reqd != null ? reqd : AbstractCsvEntityColumnMapping.DEFAULT_REQUIRED;
        this.properties =
                props != null ? Collections.unmodifiableList(new ArrayList<CsvPropertyColumnMapping<?>>(props)) : Collections.EMPTY_LIST;
    }

    @Override
    public final int getColumnIndex() {
        return columnIndex;
    }

    /**
     * Resolve the Ontological concept label for this configured entity.
     * @param fields the fields of the current line of the CSV
     * @return the ontological conceptLabel or <code>null</code> if it cannot be resolved
     */
    @JsonIgnore
    protected abstract String getConceptLabel(final List<String> fields);

    @JsonProperty("useExisting")
    public final boolean isUseExisting() {
        return useExisting;
    }

    @JsonProperty("required")
    @Override
    public final boolean isRequired() {
        return required;
    }

    @JsonProperty("properties")
    public final List<CsvPropertyColumnMapping<?>> getProperties() {
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
    @Override
    public TermMention mapTerm(final List<String> fields, final int offset, final String processId) {
        String sign = fields.get(columnIndex);
        String conceptLabel = getConceptLabel(fields);
        TermMention mention;
        if (sign == null || sign.trim().isEmpty() || conceptLabel == null) {
            if (required) {
                throw new IllegalArgumentException(String.format("Sign and Concept Label for entity in column %d are required.",
                        columnIndex));
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
    public int compareTo(final CsvEntityColumnMapping o) {
        return this.columnIndex - o.getColumnIndex();
    }
}
