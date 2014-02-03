/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.altamiracorp.lumify.mapping.column;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import java.util.List;

/**
 * A ColumnValue that always returns the same value, regardless of provided inputs.
 */
@JsonTypeName("constant")
public class ConstantColumnValue<T> implements ColumnValue<T> {
    /**
     * The value to return.
     */
    private final T value;

    /**
     * Create a new ConstantColumnValue.
     * @param val the value to return
     */
    @JsonCreator
    public ConstantColumnValue(@JsonProperty("value") final T val) {
        this.value = val;
    }

    @JsonProperty("value")
    public T getValue() {
        return value;
    }

    @Override
    public int getSortColumn() {
        return -1;
    }

    @Override
    public T getValue(final List<String> row) {
        return value;
    }

    @Override
    public int compareTo(final ColumnValue<?> o) {
        return getSortColumn() - o.getSortColumn();
    }
}
