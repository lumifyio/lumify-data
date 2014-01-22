/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.altamiracorp.lumify.storm.structuredData.mapping.csv;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class DoubleCsvPropertyColumnMappingTest {
    @Parameters(name="${index}: {0}::{1}")
    public static Iterable<Object[]> data() {
        return Arrays.asList(new Object[][] {
            { null, null },
            { "", null },
            { "\n \t\t \n", null },
            { "not a number", null },
            { "42", 42.0d },
            { "-27.23476", -27.23476d },
            { "3.14", 3.14d }
        });
    }

    private DoubleCsvPropertyColumnMapping instance;
    private final String fieldValue;
    private final Double expValue;

    public DoubleCsvPropertyColumnMappingTest(final String fieldValue, final Double expValue) {
        this.fieldValue = fieldValue;
        this.expValue = expValue;
    }

    @Before
    public void setup() {
        instance = new DoubleCsvPropertyColumnMapping(0, "name", null);
    }

    @Test
    public void testFromString() {
        assertEquals(expValue, instance.fromString(fieldValue));
    }
}
