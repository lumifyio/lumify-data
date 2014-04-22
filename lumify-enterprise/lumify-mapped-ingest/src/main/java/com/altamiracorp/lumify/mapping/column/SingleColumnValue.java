package com.altamiracorp.lumify.mapping.column;

import static com.google.common.base.Preconditions.checkArgument;

import com.altamiracorp.lumify.mapping.xform.ValueTransformer;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonTypeName;
import java.util.List;

/**
 * A ColumnValue that transforms the string found in a single
 * column to a target value type.
 * @param <T> the type of the transformed value
 */
@JsonTypeName("single")
@JsonPropertyOrder({ "index", "xform" })
public class SingleColumnValue<T> extends AbstractConvertingColumnValue<T> {
    /**
     * The index of the column whose value will be retrieved.
     */
    private final int index;

    /**
     * Create a new SingleColumnValue.
     * @param index the column index
     * @param xform the value transformer
     */
    @JsonCreator
    public SingleColumnValue(@JsonProperty("index") final int index,
            @JsonProperty(value="xform", required=false) final ValueTransformer<T> xform) {
        super(xform);
        checkArgument(index >= 0, "column index must be >= 0");
        this.index = index;
    }

    @JsonProperty("index")
    public final int getIndex() {
        return index;
    }

    @Override
    protected String resolveInputValue(final List<String> row) {
        String value;
        try {
            value = row.get(index);
        } catch (IndexOutOfBoundsException iobe) {
            value = null;
        }
        return value;
    }

    @Override
    public int getSortColumn() {
        return index;
    }
}
