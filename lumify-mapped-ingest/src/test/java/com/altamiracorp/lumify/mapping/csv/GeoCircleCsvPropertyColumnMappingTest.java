/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 *//*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 *//*
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

import com.altamiracorp.securegraph.type.GeoCircle;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class GeoCircleCsvPropertyColumnMappingTest {
    private static final String TEST_NAME = "testGeo";
    private static final int TEST_LAT_COL = 2;
    private static final int TEST_LONG_COL = 3;
    private static final int TEST_RAD_COL = 4;
    private static final Double TEST_LATITUDE = 38.8951d;
    private static final Double TEST_LONGITUDE = -77.0367d;
    private static final Double TEST_RADIUS = 3.14d;
    private static final GeoCircle TEST_CIRCLE = new GeoCircle(TEST_LATITUDE, TEST_LONGITUDE, TEST_RADIUS);

    @Test
    public void testIllegalConstruction() {
        doTestConstructor("null name", null, NullPointerException.class);
        doTestConstructor("empty name", "", IllegalArgumentException.class);
        doTestConstructor("whitespace name", "\n \t\t \n", IllegalArgumentException.class);
        doTestConstructor_LatCol("latCol < 0", -1, IllegalArgumentException.class);
        doTestConstructor_LongCol("longCol < 0", -1, IllegalArgumentException.class);
        doTestConstructor_RadCol("radCol < 0", -1, IllegalArgumentException.class);
    }

    @Test
    public void testLegalConstruction() {
        doTestConstructor("trimmed name", TEST_NAME, TEST_NAME);
        doTestConstructor("untrimmed name", "\t  " + TEST_NAME + "  \t\n", TEST_NAME);
        doTestConstructor_LatCol("latCol == 0", 0);
        doTestConstructor_LatCol("latCol > 0", 1);
        doTestConstructor_LongCol("longCol == 0", 0);
        doTestConstructor_LongCol("longCol > 0", 1);
        doTestConstructor_RadCol("radCol == 0", 0);
        doTestConstructor_RadCol("radCol > 0", 1);
        doTestConstructor("default required", null, CsvPropertyColumnMapping.DEFAULT_REQUIRED);
        doTestConstructor("required", Boolean.TRUE, true);
        doTestConstructor("!required", Boolean.FALSE, false);
    }

    @Test
    public void testGetPropertyValue_MissingFields() {
        doTestGetPropertyValue_MissingField("optional; null latitude", getInstance(false), null, ""+TEST_LONGITUDE, ""+TEST_RADIUS,
                false);
        doTestGetPropertyValue_MissingField("optional; null longitude", getInstance(false),""+TEST_LATITUDE, null, ""+TEST_RADIUS,
                false);
        doTestGetPropertyValue_MissingField("optional; null radius", getInstance(false),""+TEST_RADIUS, null, ""+TEST_RADIUS,
                false);
        doTestGetPropertyValue_MissingField("optional; latitude NaN", getInstance(false), "not a number", ""+TEST_LONGITUDE,
                ""+TEST_RADIUS, false);
        doTestGetPropertyValue_MissingField("optional; longitude NaN", getInstance(false),""+TEST_LATITUDE, "not a number",
                ""+TEST_RADIUS, false);
        doTestGetPropertyValue_MissingField("optional; radius NaN", getInstance(false),""+TEST_LATITUDE, "not a number",
                ""+TEST_RADIUS, false);
        doTestGetPropertyValue_MissingField("required; null latitude", getInstance(true), null, ""+TEST_LONGITUDE, ""+TEST_RADIUS,
                true);
        doTestGetPropertyValue_MissingField("required; null longitude", getInstance(true),""+TEST_LATITUDE, null, ""+TEST_RADIUS,
                true);
        doTestGetPropertyValue_MissingField("required; null radius", getInstance(true),""+TEST_RADIUS, null, ""+TEST_RADIUS,
                true);
        doTestGetPropertyValue_MissingField("required; latitude NaN", getInstance(true), "not a number", ""+TEST_LONGITUDE,
                ""+TEST_RADIUS, true);
        doTestGetPropertyValue_MissingField("required; longitude NaN", getInstance(true),""+TEST_LATITUDE, "not a number",
                ""+TEST_RADIUS, true);
        doTestGetPropertyValue_MissingField("required; radius NaN", getInstance(true),""+TEST_LATITUDE, "not a number",
                ""+TEST_RADIUS, true);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testGetPropertyValue() {
        List<String> fields = mock(List.class);
        when(fields.get(TEST_LAT_COL)).thenReturn(""+TEST_LATITUDE);
        when(fields.get(TEST_LONG_COL)).thenReturn(""+TEST_LONGITUDE);
        when(fields.get(TEST_RAD_COL)).thenReturn(""+TEST_RADIUS);

        GeoCircleCsvPropertyColumnMapping instance = getInstance(false);
        GeoCircle result = instance.getPropertyValue(fields);
        assertEquals(TEST_CIRCLE, result);
    }

    private GeoCircleCsvPropertyColumnMapping getInstance(final boolean required) {
        return new GeoCircleCsvPropertyColumnMapping(TEST_NAME, TEST_LAT_COL, TEST_LONG_COL, TEST_RAD_COL, required);
    }

    @SuppressWarnings("unchecked")
    private void doTestGetPropertyValue_MissingField(final String testName, final GeoCircleCsvPropertyColumnMapping instance,
            final String latStr, final String longStr, final String radStr, final boolean required) {
        List<String> fields = mock(List.class);
        when(fields.get(TEST_LAT_COL)).thenReturn(latStr);
        when(fields.get(TEST_LONG_COL)).thenReturn(longStr);
        when(fields.get(TEST_RAD_COL)).thenReturn(radStr);

        try {
            GeoCircle result = instance.getPropertyValue(fields);
            if (required) {
                fail(String.format("[%s]: Expected IllegalArgumentException for required property.", testName));
            } else {
                assertNull(String.format("[%s]: Expected null for optional property.", testName, result), result);
            }
        } catch (IllegalArgumentException iae) {
            if (!required) {
                fail(String.format("[%s]: Expected null return for optional property", testName));
            }
        }
    }

    private void doTestConstructor(final String testName, final String name, final Class<? extends Throwable> expectedError) {
        doTestConstructor(testName, name, TEST_LAT_COL, TEST_LONG_COL, TEST_RAD_COL, expectedError);
    }

    private void doTestConstructor_LatCol(final String testName, final int latCol, final Class<? extends Throwable> expectedError) {
        doTestConstructor(testName, TEST_NAME, latCol, TEST_LONG_COL, TEST_RAD_COL, expectedError);
    }

    private void doTestConstructor_LongCol(final String testName, final int longCol, final Class<? extends Throwable> expectedError) {
        doTestConstructor(testName, TEST_NAME, TEST_LAT_COL, longCol, TEST_RAD_COL, expectedError);
    }

    private void doTestConstructor_RadCol(final String testName, final Integer radCol, final Class<? extends Throwable> expectedError) {
        doTestConstructor(testName, TEST_NAME, TEST_LAT_COL, TEST_LONG_COL, radCol, expectedError);
    }

    private void doTestConstructor(final String testName, final String name, final int latCol, final int longCol, final int radCol,
            final Class<? extends Throwable> expectedError) {
        try {
            new GeoCircleCsvPropertyColumnMapping(name, latCol, longCol, radCol, null);
            fail(String.format("[%s]: Expected %s", testName, expectedError.getName()));
        } catch (Exception e) {
            assertTrue(String.format("[%s]: Expected %s, got %s", testName, expectedError.getName(), e.getClass().getName()),
                    expectedError.isAssignableFrom(e.getClass()));
        }
    }

    private void doTestConstructor(final String testName, final String name, final String expName) {
        doTestConstructor(testName, name, TEST_LAT_COL, TEST_LONG_COL, TEST_RAD_COL, null,
                expName, TEST_LAT_COL, TEST_LONG_COL, TEST_RAD_COL, CsvPropertyColumnMapping.DEFAULT_REQUIRED);
    }

    private void doTestConstructor_LatCol(final String testName, final int latCol) {
        doTestConstructor(testName, TEST_NAME, latCol, TEST_LONG_COL, TEST_RAD_COL, null,
                TEST_NAME, latCol, TEST_LONG_COL, TEST_RAD_COL, CsvPropertyColumnMapping.DEFAULT_REQUIRED);
    }

    private void doTestConstructor_LongCol(final String testName, final int longCol) {
        doTestConstructor(testName, TEST_NAME, TEST_LAT_COL, longCol, TEST_RAD_COL, null,
                TEST_NAME, TEST_LAT_COL, longCol, TEST_RAD_COL, CsvPropertyColumnMapping.DEFAULT_REQUIRED);
    }

    private void doTestConstructor_RadCol(final String testName, final int radCol) {
        doTestConstructor(testName, TEST_NAME, TEST_LAT_COL, TEST_LONG_COL, radCol, null,
                TEST_NAME, TEST_LAT_COL, TEST_LONG_COL, radCol, CsvPropertyColumnMapping.DEFAULT_REQUIRED);
    }

    private void doTestConstructor(final String testName, final Boolean required, final boolean expRequired) {
        doTestConstructor(testName, TEST_NAME, TEST_LAT_COL, TEST_LONG_COL, TEST_RAD_COL, required,
                TEST_NAME, TEST_LAT_COL, TEST_LONG_COL, TEST_RAD_COL, expRequired);
    }

    private void doTestConstructor(final String testName, final String name, final int latCol, final int longCol, final int radCol,
            final Boolean reqd, final String expName, final int expLatCol, final int expLongCol, final int expRadCol,
            final boolean expReqd) {
        GeoCircleCsvPropertyColumnMapping mapping = new GeoCircleCsvPropertyColumnMapping(name, latCol, longCol, radCol, reqd);
        assertEquals(testName, expName, mapping.getName());
        assertEquals(testName, expLatCol, mapping.getLatitudeColumnIndex());
        assertEquals(testName, expLongCol, mapping.getLongitudeColumnIndex());
        assertEquals(testName, expRadCol, mapping.getRadiusColumnIndex());
        assertEquals(testName, expReqd, mapping.isRequired());
    }
}
