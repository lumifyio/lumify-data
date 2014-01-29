package com.altamiracorp.lumify.mapping.csv;

import static com.altamiracorp.lumify.mapping.csv.EntityConceptMappedCsvRelationshipMapping.DEFAULT_SEPARATOR;
import static org.junit.Assert.*;

import com.altamiracorp.lumify.mapping.csv.EntityConceptMappedCsvRelationshipMapping.Mode;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class EntityConceptMappedCsvRelationshipMappingTest {
    private static final String TEST_SOURCE_ID = "srcId";
    private static final String TEST_TARGET_ID = "tgtId";
    private static final String NON_DEFAULT_SEPARATOR = "--";

    private static final String FOO = "foo";
    private static final String BAR = "bar";
    private static final String FIZZ = "fizz";
    private static final String BUZZ = "buzz";
    private static final String FOO_FOO_REL = "fooRelToFoo";
    private static final String FOO_BAR_REL = "fooRelToBar";
    private static final String FOO_BUZZ_REL = "fooRelToBuzz";
    private static final String FIZZ_FOO_REL = "fizzRelToFoo";
    private static final String FIZZ_BUZZ_REL = "fizzRelToBuzz";

    private static final Map<String, String> SOURCE_MAP;
    private static final Map<String, String> TARGET_MAP;
    private static final Map<String, String> DEFAULT_ALL_MAP = buildAllMap(DEFAULT_SEPARATOR);
    private static final Map<String, String> NON_DEFAULT_ALL_MAP = buildAllMap(NON_DEFAULT_SEPARATOR);

    static {
        Map<String, String> sourceMap = new HashMap<String, String>();
        sourceMap.put(FOO, FOO_BAR_REL);
        sourceMap.put(FIZZ, FIZZ_BUZZ_REL);
        SOURCE_MAP = Collections.unmodifiableMap(sourceMap);

        Map<String, String> targetMap = new HashMap<String, String>();
        targetMap.put(BAR, FOO_BAR_REL);
        targetMap.put(BUZZ, FIZZ_BUZZ_REL);
        TARGET_MAP = Collections.unmodifiableMap(targetMap);
    }

    private static Map<String, String> buildAllMap(final String sep) {
        Map<String, String> allMap = new HashMap<String, String>();
        allMap.put(toKeyStr(FOO, FOO, sep), FOO_FOO_REL);
        allMap.put(toKeyStr(FOO, BAR, sep), FOO_BAR_REL);
        allMap.put(toKeyStr(FOO, BUZZ, sep), FOO_BUZZ_REL);
        allMap.put(toKeyStr(FIZZ, BUZZ, sep), FIZZ_BUZZ_REL);
        allMap.put(toKeyStr(FIZZ, FOO, sep), FIZZ_FOO_REL);
        return Collections.unmodifiableMap(allMap);
    }

    private static String toKeyStr(final String src, final String tgt, final String sep) {
        return String.format("%s%s%s", src, sep, tgt);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testIllegalConstruction() {
        doTestConstructor("null mapMode", null, SOURCE_MAP, null, NullPointerException.class);
        doTestConstructor("null labelMap", Mode.ALL, null, null, NullPointerException.class);
        doTestConstructor("empty labelMap", Mode.ALL, Collections.EMPTY_MAP, null, IllegalArgumentException.class);
        doTestConstructor("empty separator", Mode.ALL, DEFAULT_ALL_MAP, "", IllegalArgumentException.class);
        doTestConstructor("whitespace separator", Mode.ALL, DEFAULT_ALL_MAP, "\n \t\t \n", IllegalArgumentException.class);
        doTestConstructor("invalid map keys", Mode.ALL, DEFAULT_ALL_MAP, NON_DEFAULT_SEPARATOR, IllegalArgumentException.class);
    }

    @Test
    public void testLegalConstruction() {
        doTestConstructor("source mode", Mode.SOURCE, SOURCE_MAP, null, DEFAULT_SEPARATOR);
        doTestConstructor("target mode", Mode.TARGET, TARGET_MAP, null, DEFAULT_SEPARATOR);
        doTestConstructor("default all mode", Mode.ALL, DEFAULT_ALL_MAP, null, DEFAULT_SEPARATOR);
        doTestConstructor("non-default separator", Mode.ALL, NON_DEFAULT_ALL_MAP, NON_DEFAULT_SEPARATOR, NON_DEFAULT_SEPARATOR);
        doTestConstructor("untrimmed separator", Mode.ALL, DEFAULT_ALL_MAP, "\t  " + DEFAULT_SEPARATOR + "\t\t  \n", DEFAULT_SEPARATOR);
    }

    private void doTestConstructor(final String testName, final Mode mapMode, final Map<String, String> labelMap, final String sep,
            final Class<? extends Throwable> expError) {
        try {
            new EntityConceptMappedCsvRelationshipMapping(TEST_SOURCE_ID, TEST_TARGET_ID, mapMode, labelMap, sep);
            fail(String.format("[%s]: Expected %s", testName, expError.getName()));
        } catch (Exception e) {
            assertTrue(String.format("[%s]: Expected %s, got %s", testName, expError.getName(), e.getClass().getName()),
                    expError.isAssignableFrom(e.getClass()));
        }
    }

    private void doTestConstructor(final String testName, final Mode mapMode, final Map<String, String> labelMap, final String sep,
            final String expSep) {
        EntityConceptMappedCsvRelationshipMapping instance = new EntityConceptMappedCsvRelationshipMapping(TEST_SOURCE_ID, TEST_TARGET_ID,
                mapMode, labelMap, sep);
        String msg = String.format("[%s]: ", testName);
        assertEquals(msg, mapMode, instance.getMode());
        assertEquals(msg, labelMap, instance.getLabelMap());
        assertEquals(msg, expSep, instance.getSeparator());
    }
}
