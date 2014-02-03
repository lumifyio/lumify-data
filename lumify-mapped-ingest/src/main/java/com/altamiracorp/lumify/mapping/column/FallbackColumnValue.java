package com.altamiracorp.lumify.mapping.column;

import static com.google.common.base.Preconditions.*;

import com.altamiracorp.lumify.mapping.predicate.MappingPredicate;
import com.altamiracorp.lumify.mapping.predicate.NullPredicate;
import com.altamiracorp.lumify.mapping.predicate.OrPredicate;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import java.util.List;

/**
 * This ColumnValue examines the contents of a primary ColumnValue, using
 * a secondary ColumnValue if the primary is either null or meets other
 * configured criteria.  The primary ColumnValue is used for all sort
 * comparisons.
 */
@JsonTypeName("fallback")
public class FallbackColumnValue<T> implements ColumnValue<T> {
    /**
     * The primary ColumnValue.
     */
    private final ColumnValue<T> primaryColumn;

    /**
     * The secondary ColumnValue.
     */
    private final ColumnValue<T> fallbackColumn;

    /**
     * The predicate used to trigger the fallback.
     */
    private final MappingPredicate<T> fallbackIf;

    /**
     * The fallback condition; this is configured as an OR condition
     * checking for a null primary value OR the configured fallbackIf.
     */
    private final MappingPredicate<T> condition;

    /**
     * Create a new FallbackColumnValue.
     * @param primary the primary ColumnValue
     * @param fallback the fallback ColumnValue
     * @param fallIf the condition under which the fallback value will be used (in addition to a null primary value)
     */
    @JsonCreator
    @SuppressWarnings("unchecked")
    public FallbackColumnValue(@JsonProperty("primaryColumn") final ColumnValue<T> primary,
            @JsonProperty("fallbackColumn") final ColumnValue<T> fallback,
            @JsonProperty("fallbackIf") final MappingPredicate<T> fallIf) {
        checkNotNull(primary, "primary column must be provided");
        checkNotNull(fallback, "fallback column must be provided");
        this.primaryColumn = primary;
        this.fallbackColumn = fallback;
        this.fallbackIf = fallIf;
        this.condition = new OrPredicate<T>(new NullPredicate<T>(), fallbackIf);
    }

    @JsonProperty("primaryColumn")
    public final ColumnValue<T> getPrimaryColumn() {
        return primaryColumn;
    }

    @JsonProperty("fallbackColumn")
    public final ColumnValue<T> getFallbackColumn() {
        return fallbackColumn;
    }

    @JsonProperty("fallbackIf")
    public MappingPredicate<T> getFallbackIf() {
        return fallbackIf;
    }

    @Override
    public int getSortColumn() {
        return primaryColumn.getSortColumn();
    }

    @Override
    public T getValue(final List<String> row) {
        T value;
        try {
            value = primaryColumn.getValue(row);
        } catch (IllegalArgumentException iae) {
            value = null;
        }
        if (condition.matches(value)) {
            try {
                value = fallbackColumn.getValue(row);
            } catch (IllegalArgumentException iae) {
                value = null;
            }
        }
        return value;
    }

    @Override
    public int compareTo(final ColumnValue<?> o) {
        return primaryColumn.compareTo(o);
    }
}
