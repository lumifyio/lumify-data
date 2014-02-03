/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.altamiracorp.lumify.mapping.xform;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(Parameterized.class)
public abstract class AbstractValueTransformerTest<V> {
    private final String input;
    private final V expected;
    private final ValueTransformer<V> xform;

    protected AbstractValueTransformerTest(ValueTransformer<V> xform, String input, V expected) {
        this.xform = xform;
        this.input = input;
        this.expected = expected;
    }

    @Test
    public void testTransform() {
        assertEquals(expected, xform.transform(input));
    }
}
