/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 *//*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.altamiracorp.lumify.mapping.csv;

import static org.junit.Assert.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.junit.Test;

public class StringMappedCsvPropertyColumnMappingTest {
    private static final int TEST_INDEX = 0;
    private static final String TEST_NAME = "testName";
    private static final Boolean TEST_REQUIRED = Boolean.FALSE;
    private static final Map<String, String> TEST_MAP;
    private static final String TEST_DEFAULT_VALUE = "testDefaultValue";
    private static final String TEST_NONEXISTENT_KEY = "unsetKey";
    static {
        Map<String, String> map = new HashMap<String, String>();
        map.put("Foo", "Bar");
        map.put("Fizz", "Buzz");
        TEST_MAP = Collections.unmodifiableMap(map);
    }

    @Test
    public void testFromString() {
        doTestFromString("null default value", null);
        doTestFromString("with default value", TEST_DEFAULT_VALUE);
    }

    private void doTestFromString(final String testName, final String dfltVal) {
        StringMappedCsvPropertyColumnMapping instance = new StringMappedCsvPropertyColumnMapping(TEST_INDEX, TEST_NAME, TEST_REQUIRED,
                TEST_MAP, dfltVal);
        for (String key : TEST_MAP.keySet()) {
            assertEquals(String.format("[%s]: ", testName), TEST_MAP.get(key), instance.fromString(key));
        }
        assertEquals(String.format("[%s] (default): ", testName), dfltVal, instance.fromString(TEST_NONEXISTENT_KEY));
    }
}
