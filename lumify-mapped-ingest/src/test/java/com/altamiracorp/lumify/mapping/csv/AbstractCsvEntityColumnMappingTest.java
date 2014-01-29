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
import static org.mockito.Mockito.*;

import com.altamiracorp.lumify.core.ingest.term.extraction.TermMention;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class AbstractCsvEntityColumnMappingTest {
    private static final int TEST_COLUMN = 1;
    private static final String TEST_CONCEPT_LABEL = "conceptLabel";
    private static final String TEST_SIGN = "My Test Term";
    private static final int TEST_OFFSET = 42;
    private static final String TEST_PROCESS_ID = "testProc";
    private static final String TEST_PROP_KEY1 = "prop1";
    private static final String TEST_PROP_KEY2 = "prop2";
    private static final String TEST_PROP_VALUE1 = "propValue1";
    private static final String TEST_PROP_VALUE2 = "propValue2";

    @Mock
    private CsvPropertyColumnMapping<?> prop1;
    @Mock
    private CsvPropertyColumnMapping<?> prop2;
    @Mock
    private Delegate delegate;

    private List<CsvPropertyColumnMapping<?>> propList;

    @Before
    @SuppressWarnings("unchecked")
    public void setup() {
        propList = Arrays.asList(prop1, prop2);
        when(prop1.getName()).thenReturn(TEST_PROP_KEY1);
        when(prop2.getName()).thenReturn(TEST_PROP_KEY2);
    }

    @Test
    public void testIllegalConstruction() {
        doTestConstructor("colIdx < 0", -1, IllegalArgumentException.class);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testLegalConstruction() {
        List<CsvPropertyColumnMapping<?>> singleton = new ArrayList<CsvPropertyColumnMapping<?>>();
        singleton.add(prop1);

        doTestConstructor_ColIdx("colIdx", TEST_COLUMN);
        doTestConstructor_UseExisting("null useExisting", null, AbstractCsvEntityColumnMapping.DEFAULT_USE_EXISTING);
        doTestConstructor_UseExisting("true useExisting", Boolean.TRUE, true);
        doTestConstructor_UseExisting("false useExisting", Boolean.FALSE, false);
        doTestConstructor_Required("null required", null, CsvEntityColumnMapping.DEFAULT_REQUIRED);
        doTestConstructor_Required("true required", Boolean.TRUE, true);
        doTestConstructor_Required("false required", Boolean.FALSE, false);
        doTestConstructor_Props("null props", null, Collections.EMPTY_LIST);
        doTestConstructor_Props("empty props", Collections.EMPTY_LIST);
        doTestConstructor_Props("single prop", singleton);
        doTestConstructor_Props("multiple props", propList);
    }

    @Test
    public void testMapTerm_MissingSign() {
        doMissingSignTest("null optional", null, false);
        doMissingSignTest("empty optional", "", false);
        doMissingSignTest("whitespace optional", "\n \t\t \n", false);
        doMissingSignTest("null required", null, true);
        doMissingSignTest("empty required", "", true);
        doMissingSignTest("whitespace required", "\n \t\t \n", true);
    }

    @Test
    public void testMapTerm() {
        doMapTermTest("!useExisting, no props", false, false);
        doMapTermTest("!useExisting, with props", false, true);
        doMapTermTest("useExisting, no props", true, false);
        doMapTermTest("useExisting, with props", true, true);
    }

    @Test
    public void testCompareTo() {
        AbstractCsvEntityColumnMapping idx0 = getInstance(0);
        AbstractCsvEntityColumnMapping idx1 = getInstance(1);
        AbstractCsvEntityColumnMapping idx0b = getInstance(0);

        doCompareTest("identity", idx0, idx0, 0);
        doCompareTest("equality", idx0, idx0b, 0);
        doCompareTest("A < B", idx0, idx1, -1);
        doCompareTest("A > B", idx1, idx0, 1);
    }

    private void doCompareTest(final String testName, final AbstractCsvEntityColumnMapping colMap1, final AbstractCsvEntityColumnMapping colMap2,
            final int expected) {
        int compare = colMap1.compareTo(colMap2);
        if (expected < 0) {
            assertTrue(String.format("[%s]: Expected A < B, got %d.", testName, compare), compare < 0);
        } else if (expected == 0) {
            assertTrue(String.format("[%s]: Expected A == B, got %d.", testName, compare), compare == 0);
        } else {
            assertTrue(String.format("[%s]: Expected A > B, got %d.", testName, compare), compare > 0);
        }
    }

    private AbstractCsvEntityColumnMapping getInstance(final int colIdx) {
        return new TestImpl(colIdx, null, null, null, delegate);
    }

    private AbstractCsvEntityColumnMapping getInstance(final Boolean useExisting, final List<CsvPropertyColumnMapping<?>> props,
            final Boolean required) {
        return new TestImpl(TEST_COLUMN, useExisting, props, required, delegate);
    }

    private void doMissingSignTest(final String testName, final String sign, final boolean required) {
        AbstractCsvEntityColumnMapping instance = getInstance(null, null, required);
        List<String> fields = mock(List.class);
        when(fields.get(TEST_COLUMN)).thenReturn(sign);
        try {
            TermMention result = instance.mapTerm(fields, TEST_OFFSET, TEST_PROCESS_ID);
            if (required) {
                fail(String.format("[%s]: Expected IllegalArgumentException for required sign.", testName));
            } else {
                assertNull(String.format("[%s]: Expected null TermMention for optional sign.", testName), result);
            }
        } catch (IllegalArgumentException iae) {
            assertTrue(String.format("[%s]: Expected null TermMention for optional sign; got IllegalArgumentException", testName),
                    required);
        }
    }

    private void doMapTermTest(final String testName, final boolean useExisting, final boolean withProps) {
        AbstractCsvEntityColumnMapping instance = getInstance(useExisting, withProps ? propList : null, null);
        List<String> fields = mock(List.class);
        when(fields.get(TEST_COLUMN)).thenReturn(TEST_SIGN);
        when(delegate.getConceptLabel(fields)).thenReturn(TEST_CONCEPT_LABEL);
        TermMention.Builder builder = new TermMention.Builder()
                .sign(TEST_SIGN)
                .start(TEST_OFFSET)
                .end(TEST_OFFSET + TEST_SIGN.length())
                .ontologyClassUri(TEST_CONCEPT_LABEL)
                .useExisting(useExisting)
                .resolved(true)
                .process(TEST_PROCESS_ID);
        if (withProps) {
            when(prop1.getPropertyValue(fields)).thenReturn(TEST_PROP_VALUE1);
            when(prop2.getPropertyValue(fields)).thenReturn(TEST_PROP_VALUE2);
            Map<String, Object> expectedProps = new HashMap<String, Object>();
            expectedProps.put(TEST_PROP_KEY1, TEST_PROP_VALUE1);
            expectedProps.put(TEST_PROP_KEY2, TEST_PROP_VALUE2);
            builder.properties(expectedProps);
        }
        TermMention expected = builder.build();

        assertEquals(expected, instance.mapTerm(fields, TEST_OFFSET, TEST_PROCESS_ID));
    }

    private void doTestConstructor(final String testName, final int colIdx, final Class<? extends Throwable> expectedError) {
        try {
            new TestImpl(colIdx, null, null, null, delegate);
            fail(String.format("[%s] Expected %s", testName, expectedError.getName()));
        } catch (Exception e) {
            assertTrue(String.format("[%s] Expected %s but got %s.", testName, expectedError.getName(),
                    e.getClass().getName()), expectedError.isAssignableFrom(e.getClass()));
        }
    }

    @SuppressWarnings("unchecked")
    private void doTestConstructor_ColIdx(final String testName, final int colIdx) {
        doTestConstructor(testName, colIdx, null, null, null,
                colIdx, AbstractCsvEntityColumnMapping.DEFAULT_USE_EXISTING, Collections.EMPTY_LIST,
                CsvEntityColumnMapping.DEFAULT_REQUIRED);
    }

    @SuppressWarnings("unchecked")
    private void doTestConstructor_UseExisting(final String testName, final Boolean useExisting, final boolean expUseExisting) {
        doTestConstructor(testName, TEST_COLUMN, useExisting, null, null,
                TEST_COLUMN, expUseExisting, Collections.EMPTY_LIST,
                CsvEntityColumnMapping.DEFAULT_REQUIRED);
    }

    private void doTestConstructor_Props(final String testName, final List<CsvPropertyColumnMapping<?>> props) {
        doTestConstructor_Props(testName, props, props);
    }

    private void doTestConstructor_Props(final String testName, final List<CsvPropertyColumnMapping<?>> props,
            final List<CsvPropertyColumnMapping<?>> expProps) {
        doTestConstructor(testName, TEST_COLUMN, null, props, null,
                TEST_COLUMN, AbstractCsvEntityColumnMapping.DEFAULT_USE_EXISTING, expProps,
                CsvEntityColumnMapping.DEFAULT_REQUIRED);
    }

    @SuppressWarnings("unchecked")
    private void doTestConstructor_Required(final String testName, final Boolean required, final boolean expRequired) {
        doTestConstructor(testName, TEST_COLUMN, null, null, required,
                TEST_COLUMN, AbstractCsvEntityColumnMapping.DEFAULT_USE_EXISTING, Collections.EMPTY_LIST,
                expRequired);
    }

    private void doTestConstructor(final String testName, final int colIdx, final Boolean useExisting,
            final List<CsvPropertyColumnMapping<?>> props, final Boolean required, final int expColIdx, final boolean expUseExisting,
            final List<CsvPropertyColumnMapping<?>> expProps, final boolean expRequired) {
        AbstractCsvEntityColumnMapping mapping = new TestImpl(colIdx, useExisting, props, required, delegate);
        assertEquals(testName, expColIdx, mapping.getColumnIndex());
        assertEquals(testName, expUseExisting, mapping.isUseExisting());
        assertEquals(testName, expProps, mapping.getProperties());
        assertEquals(testName, expRequired, mapping.isRequired());
    }

    private static interface Delegate {
        String getConceptLabel(final List<String> fields);
    }

    private static class TestImpl extends AbstractCsvEntityColumnMapping {
        private final Delegate delegate;

        public TestImpl(int index, Boolean useExistingIn, List<CsvPropertyColumnMapping<?>> props, Boolean reqd, Delegate dgt) {
            super(index, useExistingIn, props, reqd);
            this.delegate = dgt;
        }

        @Override
        protected String getConceptLabel(final List<String> fields) {
            return delegate.getConceptLabel(fields);
        }
    }
}
