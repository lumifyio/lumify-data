package com.altamiracorp.lumify.mapping.csv;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * An entity column mapping that resolves its concept label based on the
 * values of multiple input columns.
 */
@JsonTypeName("columnLookup")
public class ColumnLookupCsvEntityColumnMapping extends AbstractCsvEntityColumnMapping {
    /**
     * The default separator string: ":"
     */
    public static final String DEFAULT_SEPARATOR = ":";

    /**
     * The columns used to generate the lookup key.
     */
    private final List<Integer> columns;

    /**
     * The key-part separator.
     */
    private final String separator;

    /**
     * The map of joined keys to mapped values.  Keys will be
     * constructed by joining input columns in the order they
     * are specified in the mapping.
     */
    private final Map<String, String> conceptMap;

    public ColumnLookupCsvEntityColumnMapping(@JsonProperty("signColumn") final int index,
            @JsonProperty(value="useExisting", required=false) final Boolean useExisting,
            @JsonProperty("properties") final List<CsvPropertyColumnMapping<?>> properties,
            @JsonProperty(value="required", required=false) final Boolean required,
            @JsonProperty("lookupColumns") final List<Integer> lookupColumns,
            @JsonProperty("conceptMap") final Map<String, String> conMap,
            @JsonProperty(value="separator", required=false) final String sep) {
        super(index, useExisting, properties, required);
        checkNotNull(lookupColumns, "Lookup columns are required");
        checkArgument(!lookupColumns.isEmpty(), "At least one lookup column is required");
        checkNotNull(conMap, "Concept map is required");
        checkArgument(!conMap.isEmpty(), "At least one concept mapping is required");
        checkArgument(sep == null || !sep.trim().isEmpty(), "Separator must contain at least one non-whitespace character if provided");
        this.columns = Collections.unmodifiableList(new ArrayList<Integer>(lookupColumns));
        this.conceptMap = Collections.unmodifiableMap(new HashMap<String, String>(conMap));
        this.separator = sep != null ? sep.trim() : DEFAULT_SEPARATOR;
    }

    @JsonProperty("lookupColumns")
    public List<Integer> getColumns() {
        return columns;
    }

    @JsonProperty("separator")
    public String getSeparator() {
        return separator;
    }

    @JsonProperty("conceptMap")
    public Map<String, String> getConceptMap() {
        return conceptMap;
    }

    @Override
    protected String getConceptLabel(final List<String> fields) {
        String label = null;
        for (int length = columns.size(); label == null && length >= 0; length--) {
            label = conceptMap.get(buildKey(fields, length));
        }
        return label;
    }

    private String buildKey(final List<String> fields, final int length) {
        StringBuilder builder = new StringBuilder();
        int col;
        String value;
        for (int idx = 0; idx < length; idx++) {
            col = columns.get(idx);
            try {
                value = fields.get(col);
            } catch (IndexOutOfBoundsException iobe) {
                value = null;
            }
            if (builder.length() > 0) {
                builder.append(separator);
            }
            builder.append(value != null ? value : "");
        }
        return builder.toString();
    }
}
