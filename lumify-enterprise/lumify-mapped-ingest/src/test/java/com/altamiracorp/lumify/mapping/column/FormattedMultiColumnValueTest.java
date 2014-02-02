package com.altamiracorp.lumify.mapping.column;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.altamiracorp.lumify.mapping.xform.ValueTransformer;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class FormattedMultiColumnValueTest {
    private static final int COL1 = 0;
    private static final int COL2 = 3;
    private static final int COL3 = 7;
    private static final List<Integer> TEST_COLUMNS = Arrays.asList(COL1, COL2, COL3);
    private static final String TEST_FORMAT = "%s::%s::%s";

    @Mock
    private ValueTransformer<Object> xform;

    private FormattedMultiColumnValue<Object> instance;

    @Before
    public void setup() {
        instance = new FormattedMultiColumnValue<Object>(TEST_COLUMNS, TEST_FORMAT, xform);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testIllegalConstruction() {
        doTestConstructor("null columns", null, TEST_FORMAT, NullPointerException.class);
        doTestConstructor("empty columns", Collections.EMPTY_LIST, TEST_FORMAT, IllegalArgumentException.class);
        doTestConstructor("negative columns", Arrays.asList(3, 5, -2, 17), TEST_FORMAT, IllegalArgumentException.class);
        doTestConstructor("null format", TEST_COLUMNS, null, NullPointerException.class);
        doTestConstructor("empty format", TEST_COLUMNS, "", IllegalArgumentException.class);
        doTestConstructor("whitespace format", TEST_COLUMNS, "\n \t\t \n", IllegalArgumentException.class);
    }

    @Test
    public void testLegalConstruction() {
        assertEquals(TEST_COLUMNS, instance.getColumns());
        assertEquals(TEST_FORMAT, instance.getFormat());
    }

    @Test
    public void testGetSortColumn() {
        List<Integer> unsorted = Arrays.asList(COL2, COL1, COL3);
        FormattedMultiColumnValue<Object> unsortedColumns = new FormattedMultiColumnValue<Object>(unsorted, TEST_FORMAT, xform);
        assertEquals(TEST_COLUMNS.get(0).intValue(), instance.getSortColumn());
        assertEquals(unsorted.get(0).intValue(), unsortedColumns.getSortColumn());
    }

    @Test
    public void testResolveInputValue() {
        Exception ex = new IndexOutOfBoundsException();
        doTestResolveInputValue("all provided", "foo", "bar", "fizz", "foo::bar::fizz");
        doTestResolveInputValue("1, empty, 3", "foo", "", "fizz", "foo::::fizz");
        doTestResolveInputValue("1, 2, null", "foo", "bar", null, "foo::bar::");
        doTestResolveInputValue("1, ex, ex", "foo", ex, ex, "foo::::");
        doTestResolveInputValue("null, 2, null", null, "bar", null, "::bar::");
        doTestResolveInputValue("empty, null, ex", "", null, ex, null);
        doTestResolveInputValue("null, null, null", null, null, null, null);
        doTestResolveInputValue("empty, empty, empty", "", "", "", null);
        doTestResolveInputValue("ex, ex, ex", ex, ex, ex, null);
    }

    private void doTestResolveInputValue(final String testName, final Object col1val, final Object col2val, final Object col3val,
            final String expected) {
        List<String> row = mock(List.class);
        if (col1val instanceof Throwable) {
            when(row.get(COL1)).thenThrow((Throwable) col1val);
        } else {
            when(row.get(COL1)).thenReturn((String) col1val);
        }
        if (col2val instanceof Throwable) {
            when(row.get(COL2)).thenThrow((Throwable) col2val);
        } else {
            when(row.get(COL2)).thenReturn((String) col2val);
        }
        if (col3val instanceof Throwable) {
            when(row.get(COL3)).thenThrow((Throwable) col3val);
        } else {
            when(row.get(COL3)).thenReturn((String) col3val);
        }
        String resolved = instance.resolveInputValue(row);
        assertEquals(String.format("[%s]: ", testName), expected, resolved);
    }

    private void doTestConstructor(final String testName, final List<Integer> cols, final String fmt,
            final Class<? extends Throwable> expError) {
        try {
            new FormattedMultiColumnValue<Object>(cols, fmt, xform);
            fail(String.format("[%s]: Expected %s", testName, expError.getName()));
        } catch (Exception e) {
            assertTrue(String.format("[%s]: Expected %s, got %s", testName, expError.getName(), e.getClass().getName()),
                    expError.isAssignableFrom(e.getClass()));
        }
    }
}
