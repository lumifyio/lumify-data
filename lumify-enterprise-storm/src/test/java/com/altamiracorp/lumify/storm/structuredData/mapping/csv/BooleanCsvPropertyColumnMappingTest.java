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
public class BooleanCsvPropertyColumnMappingTest {
    @Parameters(name="${index}: {0}::{1}")
    public static Iterable<Object[]> data() {
        return Arrays.asList(new Object[][] {
            { null, null },
            { "", null },
            { "\n \t\t \n", null },
            { "true", true },
            { "TRUE", true },
            { "tRuE", true },
            { "false", false },
            { "FALSE", false },
            { "fAlSE", false },
            { "foo", false },
            { "BAR", false },
            { "fIzzBUzz", false }
        });
    }

    private BooleanCsvPropertyColumnMapping instance;
    private final String fieldValue;
    private final Boolean expValue;

    public BooleanCsvPropertyColumnMappingTest(final String fieldValue, final Boolean expValue) {
        this.fieldValue = fieldValue;
        this.expValue = expValue;
    }

    @Before
    public void setup() {
        instance = new BooleanCsvPropertyColumnMapping(0, "name", null);
    }

    @Test
    public void testFromString() {
        assertEquals(expValue, instance.fromString(fieldValue));
    }
}
