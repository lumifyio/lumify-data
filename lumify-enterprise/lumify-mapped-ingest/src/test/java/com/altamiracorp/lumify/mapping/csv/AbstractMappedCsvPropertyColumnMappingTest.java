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

public class AbstractMappedCsvPropertyColumnMappingTest {
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
    public void testIllegalConstruction() {
        doTestConstructor("null value map", null, null, NullPointerException.class);
    }

    @Test
    public void testLegalConstruction() {
        doTestConstructor("null default value", TEST_MAP, null);
        doTestConstructor("with default value", TEST_MAP, TEST_DEFAULT_VALUE);
    }

    @Test
    public void testFromString() {
        doTestFromString("null default value", null);
        doTestFromString("with default value", TEST_DEFAULT_VALUE);
    }

    private void doTestFromString(final String testName, final String dfltVal) {
        AbstractMappedCsvPropertyColumnMapping<String> instance = new TestImpl(TEST_INDEX, TEST_NAME, TEST_REQUIRED, TEST_MAP, dfltVal);
        for (String key : TEST_MAP.keySet()) {
            assertEquals(String.format("[%s]: ", testName), TEST_MAP.get(key), instance.fromString(key));
        }
        assertEquals(String.format("[%s] (default): ", testName), dfltVal, instance.fromString(TEST_NONEXISTENT_KEY));
    }

    private void doTestConstructor(final String testName, final Map<String, String> valMap, final String dfltVal,
            final Class<? extends Throwable> expectedError) {
        try {
            new TestImpl(TEST_INDEX, TEST_NAME, TEST_REQUIRED, valMap, dfltVal);
            fail(String.format("[%s]: Expected %s.", testName, expectedError.getName()));
        } catch (Exception e) {
            assertTrue(String.format("[%s]: Expected %s, got %s.", testName, expectedError.getName(), e.getClass().getName()),
                    expectedError.isAssignableFrom(e.getClass()));
        }
    }

    private void doTestConstructor(final String testName, final Map<String, String> valMap, final String dfltVal) {
        AbstractMappedCsvPropertyColumnMapping<String> instance = new TestImpl(TEST_INDEX, TEST_NAME, TEST_REQUIRED, valMap, dfltVal);
        assertEquals(valMap, instance.getValueMap());
        assertEquals(dfltVal, instance.getDefaultValue());
    }

    public static class TestImpl extends AbstractMappedCsvPropertyColumnMapping<String> {
        public TestImpl(int index, String name, Boolean reqd, Map<String, String> valMap, String dfltVal) {
            super(index, name, reqd, valMap, dfltVal);
        }
    }

}
