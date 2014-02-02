/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.altamiracorp.lumify.mapping.column;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import com.altamiracorp.lumify.mapping.xform.ValueTransformer;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonTypeName;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A ColumnValue that formats the values found in one or more
 * columns in the input row then uses a ValueTransformer on
 * the resulting string to extract the desired value for the
 * column.  When formatting, an empty string will be provided
 * for any columns whose input values are null or outside the
 * provided row bounds.  If all inputs are null, empty or
 * out of bounds, the value will be resolved as null.
 * @param <T> the desired output type
 */
@JsonTypeName("formattedMultiColumn")
@JsonPropertyOrder({ "indices", "format", "xform" })
public class FormattedMultiColumnValue<T> extends AbstractConvertingColumnValue<T> {
    /**
     * The column indices.  Values from these columns will be provided
     * to the format string in the order they appear in this list.
     */
    private final List<Integer> columns;

    /**
     * The format string that will be provided the values for each row.
     * This must be in a format usable by the String.format() method.
     */
    private final String format;

    /**
     * Create a new FormattedMultiColumnValue.
     * @param cols the indices of the columns that will be provided to the formatter
     * @param fmt the format string
     * @param xform the value transformer
     */
    @JsonCreator
    public FormattedMultiColumnValue(@JsonProperty("indices") final List<Integer> cols,
            @JsonProperty("format") final String fmt,
            @JsonProperty(value="xform", required=false) final ValueTransformer<T> xform) {
        super(xform);
        checkNotNull(cols, "at least one column index must be provided");
        checkArgument(!cols.isEmpty(), "at least one column index must be provided");
        for (Integer col : cols) {
            checkArgument(col >= 0, "column indices must be >= 0");
        }
        checkNotNull(fmt, "format string must be provided");
        checkArgument(!fmt.trim().isEmpty(), "format string must be provided");
        this.columns = Collections.unmodifiableList(new ArrayList<Integer>(cols));
        this.format = fmt;
    }

    @Override
    protected String resolveInputValue(final List<String> row) {
        int colCount = columns.size();
        String[] values = new String[colCount];
        boolean foundValue = false;
        for (int idx=0; idx < colCount; idx++) {
            values[idx] = getColumn(row, columns.get(idx));
            foundValue = foundValue || (values[idx] != null && !values[idx].isEmpty());
        }
        // cast values to an Object[] to trigger the varargs call
        return foundValue ? String.format(format, (Object[]) values) : null;
    }

    private String getColumn(final List<String> row, final int col) {
        String value;
        try {
            value = row.get(col);
        } catch (IndexOutOfBoundsException iobe) {
            value = null;
        }
        return value != null ? value : "";
    }

    @Override
    public int getSortColumn() {
        return columns.get(0);
    }

    @JsonProperty("indices")
    public final List<Integer> getColumns() {
        return columns;
    }

    @JsonProperty("format")
    public final String getFormat() {
        return format;
    }
}
