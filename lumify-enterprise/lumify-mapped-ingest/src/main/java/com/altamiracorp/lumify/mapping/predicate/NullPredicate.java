/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.altamiracorp.lumify.mapping.predicate;

import com.fasterxml.jackson.annotation.JsonTypeName;

/**
 * This predicate matches if the input value is <code>null</code>
 */
@JsonTypeName("isNull")
public final class NullPredicate<T> implements MappingPredicate<T> {
    @Override
    public boolean matches(final T value) {
        return value == null;
    }

    @Override
    public String toString() {
        return "? is null";
    }
}
