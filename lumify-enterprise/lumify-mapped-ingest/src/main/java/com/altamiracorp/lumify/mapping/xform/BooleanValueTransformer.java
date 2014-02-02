/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.altamiracorp.lumify.mapping.xform;

import com.fasterxml.jackson.annotation.JsonTypeName;

/**
 * This class converts input strings to Boolean values.  Null,
 * empty or whitespace only inputs will be converted to null
 * values.  All other inputs will be converted to FALSE unless
 * they are equivalent, ignoring case, to the string &quot;true&quot;.
 */
@JsonTypeName("boolean")
public class BooleanValueTransformer implements ValueTransformer<Boolean> {
    @Override
    public Boolean transform(final String input) {
        Boolean value = null;
        if (input != null && !input.trim().isEmpty()) {
            value = Boolean.parseBoolean(input.trim());
        }
        return value;
    }
}
