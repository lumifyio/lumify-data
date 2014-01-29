/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.altamiracorp.lumify.mapping.csv;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class DateCsvPropertyColumnMappingTest {
    private static final String TEST_FORMAT = "yyyyMMdd-HHmm";

    @Parameters(name="${index}: format({0}) {1}::{2}")
    public static Iterable<Object[]> data() {
        return Arrays.asList(new Object[][] {
            { null, null, null },
            { null, "", null },
            { null, "\n \t\t \n", null },
            { null, "not a date", null },
            { null, "07/07/1979 17:03:10", 300229390000L },
            { null, "10/15/2012", 1350273600000L },
            { TEST_FORMAT, null, null },
            { TEST_FORMAT, "", null },
            { TEST_FORMAT, "\n \t\t \n", null },
            { TEST_FORMAT, "not a date", null },
            { TEST_FORMAT, "20110116-1600", 1295211600000L }
        });
    }

    private final String format;
    private final String fieldValue;
    private final Long expValue;
    private DateCsvPropertyColumnMapping instance;

    public DateCsvPropertyColumnMappingTest(final String format, final String fieldValue, final Long expValue) {
        this.format = format;
        this.fieldValue = fieldValue;
        this.expValue = expValue;
    }

    @Before
    public void setup() {
        instance = new DateCsvPropertyColumnMapping(0, "name", null, format);
    }

    @Test
    public void testFromString() {
        assertEquals(expValue, instance.fromString(fieldValue));
    }
}
