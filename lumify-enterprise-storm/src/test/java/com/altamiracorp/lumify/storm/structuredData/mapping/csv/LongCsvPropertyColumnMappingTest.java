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
public class LongCsvPropertyColumnMappingTest {
    @Parameters(name="${index}: {0}::{1}")
    public static Iterable<Object[]> data() {
        return Arrays.asList(new Object[][] {
            { null, null },
            { "", null },
            { "\n \t\t \n", null },
            { "not a number", null },
            { "42", 42L },
            { "-27", -27L },
            { "3", 3L },
            { "1390425839988", 1390425839988L }
        });
    }

    private LongCsvPropertyColumnMapping instance;
    private final String fieldValue;
    private final Long expValue;

    public LongCsvPropertyColumnMappingTest(final String fieldValue, final Long expValue) {
        this.fieldValue = fieldValue;
        this.expValue = expValue;
    }

    @Before
    public void setup() {
        instance = new LongCsvPropertyColumnMapping(0, "name", null);
    }

    @Test
    public void testFromString() {
        assertEquals(expValue, instance.fromString(fieldValue));
    }
}
