/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.altamiracorp.lumify.storm.structuredData.mapping.csv;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class AbstractSimpleCsvPropertyColumnMappingTest {
    private static final int TEST_COLUMN = 1;
    private static final String TEST_NAME = "testName";
    private static final Boolean TEST_REQUIRED = Boolean.TRUE;
    private static final String TEST_FIELD = "testField";
    private static final Object TEST_VALUE = "testValue";

    @Mock
    private Delegate delegate;

    @Test
    public void testIllegalConstruction() {
        doTestConstructor("column < 0", -1, IllegalArgumentException.class);
        doTestConstructor("null name", null, NullPointerException.class);
        doTestConstructor("empty name", "", IllegalArgumentException.class);
        doTestConstructor("whitespace name", "\n \t\t \n", IllegalArgumentException.class);
    }

    @Test
    public void testLegalConstruction() {
        doTestConstructor("column = 0", 0);
        doTestConstructor("column > 0", 1);
        doTestConstructor("trimmed name", TEST_NAME, TEST_NAME);
        doTestConstructor("untrimmed name", "\t  " + TEST_NAME + "  \t\n", TEST_NAME);
        doTestConstructor("null required", null, CsvPropertyColumnMapping.DEFAULT_REQUIRED);
        doTestConstructor("required", Boolean.TRUE, true);
        doTestConstructor("!required", Boolean.FALSE, false);
    }

    @Test
    public void testGetPropertyValue() {
        doTestGetPropertyValue("null optional", true, false);
        doTestGetPropertyValue("non-null optional", false, false);
        doTestGetPropertyValue("null required", true, true);
        doTestGetPropertyValue("non-null required", false, true);
    }

    @SuppressWarnings("unchecked")
    private void doTestGetPropertyValue(final String testName, final boolean nullValue, final boolean required) {
        List<String> fields = mock(List.class);
        when(fields.get(TEST_COLUMN)).thenReturn(TEST_FIELD);
        when(delegate.fromString(TEST_FIELD)).thenReturn(nullValue ? null : TEST_VALUE);
        AbstractSimpleCsvPropertyColumnMapping<Object> instance =
                new DelegateSimpleCsvPropertyColumnMapping(TEST_COLUMN, TEST_NAME, required);
        try {
            Object value = instance.getPropertyValue(fields);
            if (nullValue) {
                if (required) {
                    fail(String.format("[%s]: Expected IllegalArgumentException for required null property.", testName));
                } else {
                    assertNull(String.format("[%s]: Returned optional value should be null.", testName), value);
                }
            } else {
                assertEquals(TEST_VALUE, value);
            }
        } catch (IllegalArgumentException iae) {
            if (!(nullValue && required)) {
                fail(String.format("[%s]: Unexpected IllegalArgumentException.", testName));
            }
        }
    }

    private void doTestConstructor(final String testName, final int colIdx, final Class<? extends Throwable> expectedError) {
        doTestConstructor(testName, colIdx, TEST_NAME, TEST_REQUIRED, expectedError);
    }

    private void doTestConstructor(final String testName, final String name, final Class<? extends Throwable> expectedError) {
        doTestConstructor(testName, TEST_COLUMN, name, TEST_REQUIRED, expectedError);
    }

    private void doTestConstructor(final String testName, final int colIdx, final String name, final Boolean required,
            final Class<? extends Throwable> expectedError) {
        try {
            new DelegateSimpleCsvPropertyColumnMapping(colIdx, name, required);
            fail(String.format("[%s]: Expected %s", testName, expectedError.getName()));
        } catch (Exception e) {
            assertTrue(String.format("[%s]: Expected %s, got %s", testName, expectedError.getName(), e.getClass().getName()),
                    expectedError.isAssignableFrom(e.getClass()));
        }
    }

    private void doTestConstructor(final String testName, final int colIdx) {
        doTestConstructor(testName, colIdx, TEST_NAME, TEST_REQUIRED, colIdx, TEST_NAME, TEST_REQUIRED);
    }

    private void doTestConstructor(final String testName, final String name, final String expName) {
        doTestConstructor(testName, TEST_COLUMN, name, TEST_REQUIRED, TEST_COLUMN, expName, TEST_REQUIRED);
    }

    private void doTestConstructor(final String testName, final Boolean required, final boolean expRequired) {
        doTestConstructor(testName, TEST_COLUMN, TEST_NAME, required, TEST_COLUMN, TEST_NAME, expRequired);
    }

    private void doTestConstructor(final String testName, final int colIdx, final String name, final Boolean required,
            final int expColIdx, final String expName, final boolean expRequired) {
        AbstractSimpleCsvPropertyColumnMapping mapping = new DelegateSimpleCsvPropertyColumnMapping(colIdx, name, required);
        assertEquals(testName, expColIdx, mapping.getColumnIndex());
        assertEquals(testName, expName, mapping.getName());
        assertEquals(testName, expRequired, mapping.isRequired());
    }

    protected interface Delegate {
        Object fromString(final String fieldValue);
    }

    protected class DelegateSimpleCsvPropertyColumnMapping extends AbstractSimpleCsvPropertyColumnMapping<Object> {
        public DelegateSimpleCsvPropertyColumnMapping(int index, String nm, Boolean reqd) {
            super(index, nm, reqd);
        }

        @Override
        protected Object fromString(final String fieldValue) {
            return delegate.fromString(fieldValue);
        }
    }
}
