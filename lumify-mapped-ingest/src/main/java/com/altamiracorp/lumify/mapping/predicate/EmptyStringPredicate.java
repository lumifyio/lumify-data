/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

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
