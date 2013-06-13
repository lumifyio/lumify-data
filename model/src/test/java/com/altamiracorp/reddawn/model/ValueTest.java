package com.altamiracorp.reddawn.model;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static org.junit.Assert.assertEquals;

@RunWith(JUnit4.class)
public class ValueTest {
    @Test
    public void testLongConversion() {
        for (long i = 0; i < 10000; i++) {
            Value value = new Value(new Long(i));
            assertEquals(i, value.toLong().longValue());
        }
    }
}
