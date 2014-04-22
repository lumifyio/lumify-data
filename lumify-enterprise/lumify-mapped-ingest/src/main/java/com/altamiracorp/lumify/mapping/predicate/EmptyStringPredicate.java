package com.altamiracorp.lumify.mapping.predicate;

import com.fasterxml.jackson.annotation.JsonTypeName;

/**
 * This predicate matches Strings whose values are empty or whitespace-only.
 */
@JsonTypeName("emptyStr")
public class EmptyStringPredicate implements MappingPredicate<String> {
    @Override
    public boolean matches(final String value) {
        return value != null && value.trim().isEmpty();
    }

    @Override
    public String toString() {
        return "? eq \"\"";
    }
}
