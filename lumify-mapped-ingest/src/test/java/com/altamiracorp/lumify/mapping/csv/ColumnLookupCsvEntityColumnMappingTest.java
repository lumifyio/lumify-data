package com.altamiracorp.lumify.mapping.csv;

import static com.altamiracorp.lumify.mapping.csv.ColumnLookupCsvEntityColumnMapping.DEFAULT_SEPARATOR;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ColumnLookupCsvEntityColumnMappingTest {
    private static final int TEST_SIGN_COLUMN = 0;
    private static final Boolean TEST_USE_EXISTING = Boolean.TRUE;
    @SuppressWarnings("unchecked")
    private static final List<CsvPropertyColumnMapping<?>> TEST_PROPERTIES = Collections.EMPTY_LIST;
    private static final Boolean TEST_REQUIRED = Boolean.FALSE;
    private static final String NON_DEFAULT_SEPARATOR = "-";

    private static final Integer COL1 = 1;
    private static final Integer COL2 = 5;
    private static final Integer COL3 = 7;
    private static final List<Integer> TEST_COLUMNS = Arrays.asList(COL1, COL2, COL3);

    private static final String VALUE1 = "value1";
    private static final String VALUE2 = "value2";
    private static final String VALUE3 = "value3";
    private static final String VALUE4 = "value4";
    private static final String VALUE5 = "value5";

    private static final String DEFAULT_LABEL = "defaultLabel";
    private static final String V1_LABEL = "valueLabel[1]";
    private static final String V12_LABEL = "valueLabel[1,2]";
    private static final String V123_LABEL = "valueLabel[1,2,3]";
    private static final String V1_3_LABEL = "valueLabel[1,-,3]";
    private static final String V_23_LABEL = "valueLabel[-,2,3]";

    private static final Map<String, String> DEF_SEP_NO_DEF_MAP = buildConceptMap(DEFAULT_SEPARATOR, false);
    private static final Map<String, String> DEF_SEP_WITH_DEF_MAP = buildConceptMap(DEFAULT_SEPARATOR, true);
    private static final Map<String, String> NON_DEF_SEP_NO_DEF_MAP = buildConceptMap(NON_DEFAULT_SEPARATOR, false);

    private static Map<String, String> buildConceptMap(final String sep, final boolean addDefault) {
        Map<String, String> map = new HashMap<String, String>();
        if (addDefault) {
            map.put("", DEFAULT_LABEL);
        }
        map.put(buildKey(sep, VALUE1), V1_LABEL);
        map.put(buildKey(sep, VALUE1, VALUE2), V12_LABEL);
        map.put(buildKey(sep, VALUE1, VALUE2, VALUE3), V123_LABEL);
        map.put(buildKey(sep, VALUE1, "", VALUE3), V1_3_LABEL);
        map.put(buildKey(sep, "", VALUE2, VALUE3), V_23_LABEL);
        return Collections.unmodifiableMap(map);
    }

    private static String buildKey(final String sep, final String... values) {
        StringBuilder builder = new StringBuilder();
        for (String val : values) {
            if (builder.length() > 0) {
                builder.append(sep);
            }
            builder.append(val);
        }
        return builder.toString();
    }

    @Before
    public void setup() {
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testIllegalConstruction() {
        doTestConstructor("null columns", (List<Integer>) null, NullPointerException.class);
        doTestConstructor("empty columns", Collections.EMPTY_LIST, IllegalArgumentException.class);
        doTestConstructor("null concept map", (Map<String, String>) null, NullPointerException.class);
        doTestConstructor("empty concept map", Collections.EMPTY_MAP, IllegalArgumentException.class);
        doTestConstructor("empty separator", "", IllegalArgumentException.class);
        doTestConstructor("whitespace separator", "\n \t\t \n", IllegalArgumentException.class);
    }

    @Test
    public void testLegalConstruction() {
        doTestConstructor("null separator", null, DEFAULT_SEPARATOR);
        doTestConstructor("trimmed separator", NON_DEFAULT_SEPARATOR, NON_DEFAULT_SEPARATOR);
        doTestConstructor("untrimmed separator", "\t  " + NON_DEFAULT_SEPARATOR + "   \n", NON_DEFAULT_SEPARATOR);
    }

    @Test
    public void testGetConceptLabel() {
        Throwable ex = new IndexOutOfBoundsException();
        doTestGetConceptLabel("null values, no default", DEF_SEP_NO_DEF_MAP, DEFAULT_SEPARATOR, valMap(null, null, null), null);
        doTestGetConceptLabel("null values, with default", DEF_SEP_WITH_DEF_MAP, DEFAULT_SEPARATOR, valMap(null, null, null), DEFAULT_LABEL);
        doTestGetConceptLabel("exceptions, no default", DEF_SEP_NO_DEF_MAP, DEFAULT_SEPARATOR, valMap(ex, ex, ex), null);
        doTestGetConceptLabel("exceptions, with default", DEF_SEP_WITH_DEF_MAP, DEFAULT_SEPARATOR, valMap(ex, ex, ex),
                DEFAULT_LABEL);
        doTestGetConceptLabel("no mapping, no default", DEF_SEP_NO_DEF_MAP, DEFAULT_SEPARATOR, valMap(VALUE3, VALUE4, VALUE5), null);
        doTestGetConceptLabel("no mapping, with default", DEF_SEP_WITH_DEF_MAP, DEFAULT_SEPARATOR, valMap(VALUE3, VALUE4, VALUE5),
                DEFAULT_LABEL);
        doTestGetConceptLabel("v1,-,-", DEF_SEP_WITH_DEF_MAP, DEFAULT_SEPARATOR, valMap(VALUE1, null, null), V1_LABEL);
        doTestGetConceptLabel("v1,v2,-", NON_DEF_SEP_NO_DEF_MAP, NON_DEFAULT_SEPARATOR, valMap(VALUE1, VALUE2, null), V12_LABEL);
        doTestGetConceptLabel("v1,v2,v3", NON_DEF_SEP_NO_DEF_MAP, NON_DEFAULT_SEPARATOR, valMap(VALUE1, VALUE2, VALUE3), V123_LABEL);
        doTestGetConceptLabel("v1,-,v3", DEF_SEP_WITH_DEF_MAP, DEFAULT_SEPARATOR, valMap(VALUE1, null, VALUE3), V1_3_LABEL);
        doTestGetConceptLabel("v1,ex,v3", DEF_SEP_WITH_DEF_MAP, DEFAULT_SEPARATOR, valMap(VALUE1, ex, VALUE3), V1_3_LABEL);
        doTestGetConceptLabel("-,v2,v3", DEF_SEP_WITH_DEF_MAP, DEFAULT_SEPARATOR, valMap(null, VALUE2, VALUE3), V_23_LABEL);
        doTestGetConceptLabel("ex,v2,v3", DEF_SEP_WITH_DEF_MAP, DEFAULT_SEPARATOR, valMap(ex, VALUE2, VALUE3), V_23_LABEL);
        doTestGetConceptLabel("v1,v4,v5", DEF_SEP_NO_DEF_MAP, DEFAULT_SEPARATOR, valMap(VALUE1, VALUE4, VALUE5), V1_LABEL);
        doTestGetConceptLabel("v1,v2,v5", DEF_SEP_NO_DEF_MAP, DEFAULT_SEPARATOR, valMap(VALUE1, VALUE2, VALUE5), V12_LABEL);
    }

    private Map<Integer, Object> valMap(final Object col1val, final Object col2val, final Object col3val) {
        Map<Integer, Object> map = new HashMap<Integer, Object>();
        map.put(COL1, col1val);
        map.put(COL2, col2val);
        map.put(COL3, col3val);
        return map;
    }

    @SuppressWarnings("unchecked")
    private void doTestGetConceptLabel(final String testName, final Map<String, String> conMap, final String sep,
            final Map<Integer, Object> valMap, final String expLabel) {
        List<String> fields = mock(List.class);
        for (Map.Entry<Integer, Object> entry : valMap.entrySet()) {
            if (entry.getValue() instanceof Throwable) {
                when(fields.get(entry.getKey())).thenThrow((Throwable) entry.getValue());
            } else {
                when(fields.get(entry.getKey())).thenReturn((String) entry.getValue());
            }
        }
        ColumnLookupCsvEntityColumnMapping instance = new ColumnLookupCsvEntityColumnMapping(TEST_SIGN_COLUMN, TEST_USE_EXISTING,
                TEST_PROPERTIES, TEST_REQUIRED, TEST_COLUMNS, conMap, sep);
        String label = instance.getConceptLabel(fields);
        assertEquals(String.format("[%s]: ", testName), expLabel, label);
    }

    private void doTestConstructor(final String testName, final List<Integer> cols, final Class<? extends Throwable> expError) {
        doTestConstructor(testName, cols, DEF_SEP_NO_DEF_MAP, null, expError);
    }

    private void doTestConstructor(final String testName, final Map<String, String> conMap, final Class<? extends Throwable> expError) {
        doTestConstructor(testName, TEST_COLUMNS, conMap, null, expError);
    }

    private void doTestConstructor(final String testName, final String sep, final Class<? extends Throwable> expError) {
        doTestConstructor(testName, TEST_COLUMNS, DEF_SEP_NO_DEF_MAP, sep, expError);
    }

    private void doTestConstructor(final String testName, final List<Integer> cols, final Map<String, String> conMap, final String sep,
            final Class<? extends Throwable> expError) {
        try {
            new ColumnLookupCsvEntityColumnMapping(TEST_SIGN_COLUMN, TEST_USE_EXISTING, TEST_PROPERTIES, TEST_REQUIRED, cols, conMap, sep);
            fail(String.format("[%s]: Expected %s", testName, expError.getClass()));
        } catch (Exception e) {
            assertTrue(String.format("[%s]: Expected %s, got %s", testName, expError.getName(), e.getClass().getName()),
                    expError.isAssignableFrom(e.getClass()));
        }
    }

    private void doTestConstructor(final String testName, final String sep, final String expSep) {
        doTestConstructor(testName, TEST_COLUMNS, DEF_SEP_NO_DEF_MAP, sep, TEST_COLUMNS, DEF_SEP_NO_DEF_MAP, expSep);
    }

    private void doTestConstructor(final String testName, final List<Integer> cols, final Map<String, String> conMap, final String sep,
            final List<Integer> expCols, final Map<String, String> expConMap, final String expSep) {
        ColumnLookupCsvEntityColumnMapping instance = new ColumnLookupCsvEntityColumnMapping(TEST_SIGN_COLUMN, TEST_USE_EXISTING,
                TEST_PROPERTIES, TEST_REQUIRED, cols, conMap, sep);
        String msg = String.format("[%s]: ", testName);
        assertEquals(msg, expCols, instance.getColumns());
        assertEquals(msg, expConMap, instance.getConceptMap());
        assertEquals(msg, expSep, instance.getSeparator());
    }
}
