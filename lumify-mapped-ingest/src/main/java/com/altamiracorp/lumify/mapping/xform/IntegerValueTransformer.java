/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.altamiracorp.lumify.mapping.xform;

import com.fasterxml.jackson.annotation.JsonTypeName;

/**
 * This class attempts to parse an input string as an Integer,
 * returning the resulting value or null if it cannot be parsed.
 */
@JsonTypeName("integer")
public class IntegerValueTransformer implements ValueTransformer<Integer> {
    @Override
    public Integer transform(final String input) {
        Integer value;
        try {
            value = Integer.valueOf(input.trim());
        } catch (NumberFormatException nfe) {
            value = null;
        } catch (NullPointerException npe) {
            value = null;
        }
        return value;
    }
}
