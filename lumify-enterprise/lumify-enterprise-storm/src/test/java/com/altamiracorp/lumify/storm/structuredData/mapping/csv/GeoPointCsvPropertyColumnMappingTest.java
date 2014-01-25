/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.altamiracorp.lumify.storm.structuredData.mapping.csv;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import com.altamiracorp.securegraph.type.GeoPoint;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class GeoPointCsvPropertyColumnMappingTest {
    private static final String TEST_NAME = "testGeo";
    private static final int TEST_LAT_COL = 2;
    private static final int TEST_LONG_COL = 3;
    private static final int TEST_ALT_COL = 4;
    private static final Double TEST_LATITUDE = 38.8951d;
    private static final Double TEST_LONGITUDE = -77.0367d;
    private static final Double TEST_ALTITUDE = 114.91d;
    private static final GeoPoint NO_ALT_POINT = new GeoPoint(TEST_LATITUDE, TEST_LONGITUDE);
    private static final GeoPoint WITH_ALT_POINT = new GeoPoint(TEST_LATITUDE, TEST_LONGITUDE, TEST_ALTITUDE);

    @Test
    public void testIllegalConstruction() {
        doTestConstructor("null name", null, NullPointerException.class);
        doTestConstructor("empty name", "", IllegalArgumentException.class);
        doTestConstructor("whitespace name", "\n \t\t \n", IllegalArgumentException.class);
        doTestConstructor_LatCol("latCol < 0", -1, IllegalArgumentException.class);
        doTestConstructor_LongCol("longCol < 0", -1, IllegalArgumentException.class);
        doTestConstructor_AltCol("altCol < 0", -1, IllegalArgumentException.class);
    }

    @Test
    public void testLegalConstruction() {
        doTestConstructor("trimmed name", TEST_NAME, TEST_NAME);
        doTestConstructor("untrimmed name", "\t  " + TEST_NAME + "  \t\n", TEST_NAME);
        doTestConstructor_LatCol("latCol == 0", 0);
        doTestConstructor_LatCol("latCol > 0", 1);
        doTestConstructor_LongCol("longCol == 0", 0);
        doTestConstructor_LongCol("longCol > 0", 1);
        doTestConstructor_AltCol("altCol == null", null);
        doTestConstructor_AltCol("altCol == 0", 0);
        doTestConstructor_AltCol("altCol > 0", 1);
        doTestConstructor("default required", null, CsvPropertyColumnMapping.DEFAULT_REQUIRED);
        doTestConstructor("required", Boolean.TRUE, true);
        doTestConstructor("!required", Boolean.FALSE, false);
    }

    @Test
    public void testGetPropertyValue() {
        doTestGetPropertyValue_MissingField("optional; null latitude", getInstance(false, false), null, ""+TEST_LONGITUDE, ""+TEST_ALTITUDE,
                false);
        doTestGetPropertyValue_MissingField("optional; null longitude", getInstance(false, false),""+TEST_LATITUDE, null, ""+TEST_ALTITUDE,
                false);
        doTestGetPropertyValue_MissingField("optional; null altitude", getInstance(true, false),""+TEST_ALTITUDE, null, ""+TEST_ALTITUDE,
                false);
        doTestGetPropertyValue_MissingField("optional; latitude NaN", getInstance(false, false), "not a number", ""+TEST_LONGITUDE,
                ""+TEST_ALTITUDE, false);
        doTestGetPropertyValue_MissingField("optional; longitude NaN", getInstance(false, false),""+TEST_LATITUDE, "not a number",
                ""+TEST_ALTITUDE, false);
        doTestGetPropertyValue_MissingField("optional; altitude NaN", getInstance(true, false),""+TEST_LATITUDE, "not a number",
                ""+TEST_ALTITUDE, false);
        doTestGetPropertyValue_MissingField("required; null latitude", getInstance(false, true), null, ""+TEST_LONGITUDE, ""+TEST_ALTITUDE,
                true);
        doTestGetPropertyValue_MissingField("required; null longitude", getInstance(false, true),""+TEST_LATITUDE, null, ""+TEST_ALTITUDE,
                true);
        doTestGetPropertyValue_MissingField("required; null altitude", getInstance(true, true),""+TEST_ALTITUDE, null, ""+TEST_ALTITUDE,
                true);
        doTestGetPropertyValue_MissingField("required; latitude NaN", getInstance(false, true), "not a number", ""+TEST_LONGITUDE,
                ""+TEST_ALTITUDE, true);
        doTestGetPropertyValue_MissingField("required; longitude NaN", getInstance(false, true),""+TEST_LATITUDE, "not a number",
                ""+TEST_ALTITUDE, true);
        doTestGetPropertyValue_MissingField("required; altitude NaN", getInstance(true, true),""+TEST_LATITUDE, "not a number",
                ""+TEST_ALTITUDE, true);
        doTestGetPropertyValue("no altitude", false);
        doTestGetPropertyValue("with altitude", true);
    }

    private GeoPointCsvPropertyColumnMapping getInstance(final boolean withAlt, final boolean required) {
        return new GeoPointCsvPropertyColumnMapping(TEST_NAME, TEST_LAT_COL, TEST_LONG_COL, withAlt ? TEST_ALT_COL : null, required);
    }

    @SuppressWarnings("unchecked")
    private void doTestGetPropertyValue_MissingField(final String testName, final GeoPointCsvPropertyColumnMapping instance,
            final String latStr, final String longStr, final String altStr, final boolean required) {
        List<String> fields = mock(List.class);
        when(fields.get(TEST_LAT_COL)).thenReturn(latStr);
        when(fields.get(TEST_LONG_COL)).thenReturn(longStr);
        when(fields.get(TEST_ALT_COL)).thenReturn(altStr);

        try {
            GeoPoint result = instance.getPropertyValue(fields);
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

    private void doTestGetPropertyValue(final String testName, final boolean withAlt) {
        List<String> fields = mock(List.class);
        when(fields.get(TEST_LAT_COL)).thenReturn(""+TEST_LATITUDE);
        when(fields.get(TEST_LONG_COL)).thenReturn(""+TEST_LONGITUDE);
        when(fields.get(TEST_ALT_COL)).thenReturn(""+TEST_ALTITUDE);

        GeoPointCsvPropertyColumnMapping instance = getInstance(withAlt, false);
        GeoPoint result = instance.getPropertyValue(fields);
        assertEquals(testName, withAlt ? WITH_ALT_POINT : NO_ALT_POINT, result);
    }

    private void doTestConstructor(final String testName, final String name, final Class<? extends Throwable> expectedError) {
        doTestConstructor(testName, name, TEST_LAT_COL, TEST_LONG_COL, null, expectedError);
    }

    private void doTestConstructor_LatCol(final String testName, final int latCol, final Class<? extends Throwable> expectedError) {
        doTestConstructor(testName, TEST_NAME, latCol, TEST_LONG_COL, null, expectedError);
    }

    private void doTestConstructor_LongCol(final String testName, final int longCol, final Class<? extends Throwable> expectedError) {
        doTestConstructor(testName, TEST_NAME, TEST_LAT_COL, longCol, null, expectedError);
    }

    private void doTestConstructor_AltCol(final String testName, final Integer altCol, final Class<? extends Throwable> expectedError) {
        doTestConstructor(testName, TEST_NAME, TEST_LAT_COL, TEST_LONG_COL, altCol, expectedError);
    }

    private void doTestConstructor(final String testName, final String name, final int latCol, final int longCol, final Integer altCol,
            final Class<? extends Throwable> expectedError) {
        try {
            new GeoPointCsvPropertyColumnMapping(name, latCol, longCol, altCol, null);
            fail(String.format("[%s]: Expected %s", testName, expectedError.getName()));
        } catch (Exception e) {
            assertTrue(String.format("[%s]: Expected %s, got %s", testName, expectedError.getName(), e.getClass().getName()),
                    expectedError.isAssignableFrom(e.getClass()));
        }
    }

    private void doTestConstructor(final String testName, final String name, final String expName) {
        doTestConstructor(testName, name, TEST_LAT_COL, TEST_LONG_COL, null, null,
                expName, TEST_LAT_COL, TEST_LONG_COL, null, CsvPropertyColumnMapping.DEFAULT_REQUIRED);
    }

    private void doTestConstructor_LatCol(final String testName, final int latCol) {
        doTestConstructor(testName, TEST_NAME, latCol, TEST_LONG_COL, null, null,
                TEST_NAME, latCol, TEST_LONG_COL, null, CsvPropertyColumnMapping.DEFAULT_REQUIRED);
    }

    private void doTestConstructor_LongCol(final String testName, final int longCol) {
        doTestConstructor(testName, TEST_NAME, TEST_LAT_COL, longCol, null, null,
                TEST_NAME, TEST_LAT_COL, longCol, null, CsvPropertyColumnMapping.DEFAULT_REQUIRED);
    }

    private void doTestConstructor_AltCol(final String testName, final Integer altCol) {
        doTestConstructor(testName, TEST_NAME, TEST_LAT_COL, TEST_LONG_COL, altCol, null,
                TEST_NAME, TEST_LAT_COL, TEST_LONG_COL, altCol, CsvPropertyColumnMapping.DEFAULT_REQUIRED);
    }

    private void doTestConstructor(final String testName, final Boolean required, final boolean expRequired) {
        doTestConstructor(testName, TEST_NAME, TEST_LAT_COL, TEST_LONG_COL, null, required,
                TEST_NAME, TEST_LAT_COL, TEST_LONG_COL, null, expRequired);
    }

    private void doTestConstructor(final String testName, final String name, final int latCol, final int longCol, final Integer altCol,
            final Boolean reqd, final String expName, final int expLatCol, final int expLongCol, final Integer expAltCol,
            final boolean expReqd) {
        GeoPointCsvPropertyColumnMapping mapping = new GeoPointCsvPropertyColumnMapping(name, latCol, longCol, altCol, reqd);
        assertEquals(testName, expName, mapping.getName());
        assertEquals(testName, expLatCol, mapping.getLatitudeColumnIndex());
        assertEquals(testName, expLongCol, mapping.getLongitudeColumnIndex());
        assertEquals(testName, expAltCol, mapping.getAltitudeColumnIndex());
        assertEquals(testName, expReqd, mapping.isRequired());
    }
}
