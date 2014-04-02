/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.altamiracorp.lumify.clavin;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import com.altamiracorp.lumify.clavin.SimpleClavinOntologyMapper;
import com.bericotech.clavin.gazetteer.FeatureClass;
import com.bericotech.clavin.gazetteer.FeatureCode;
import com.bericotech.clavin.gazetteer.GeoName;
import com.bericotech.clavin.resolver.ResolvedLocation;
import java.util.Arrays;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class SimpleClavinOntologyMapperTest {
    private static final String DEFAULT_VALUE = "defaultValue";
    private static final String CITY = "http://lumify.io/dev#city";
    private static final String COUNTRY = "http://lumify.io/dev#country";
    private static final String STATE = "http://lumify.io/dev#state";

    @Parameters(name="${index}: getUri([{0},{1}],{2})={3}")
    public static Iterable<Object[]> data() {
        return Arrays.asList(new Object[][] {
            { null, null, null, null },
            { null, null, DEFAULT_VALUE, DEFAULT_VALUE },
            { FeatureClass.NULL, FeatureCode.ADM1, null, null },
            { FeatureClass.NULL, FeatureCode.ADM1, DEFAULT_VALUE, DEFAULT_VALUE },
            { FeatureClass.A, FeatureCode.ADM1, DEFAULT_VALUE, STATE },
            { FeatureClass.A, FeatureCode.PCLI, DEFAULT_VALUE, COUNTRY },
            { FeatureClass.A, FeatureCode.PCL, DEFAULT_VALUE, DEFAULT_VALUE },
            { FeatureClass.A, null, DEFAULT_VALUE, DEFAULT_VALUE },
            { FeatureClass.A, FeatureCode.NULL, DEFAULT_VALUE, DEFAULT_VALUE },
            { FeatureClass.H, FeatureCode.NULL, DEFAULT_VALUE, DEFAULT_VALUE },
            { FeatureClass.P, null, DEFAULT_VALUE, CITY },
            { FeatureClass.P, FeatureCode.ADM1, DEFAULT_VALUE, CITY },
            { FeatureClass.P, FeatureCode.BNCH, DEFAULT_VALUE, CITY },
            { FeatureClass.P, FeatureCode.NULL, DEFAULT_VALUE, CITY }
        });
    }

    private final FeatureClass featureClass;
    private final FeatureCode featureCode;
    private final String defaultValue;
    private final String expectedUri;
    private final ResolvedLocation location;

    private final SimpleClavinOntologyMapper instance;

    public SimpleClavinOntologyMapperTest(FeatureClass featureClass, FeatureCode featureCode, String defaultValue, String expectedUri) {
        this.featureClass = featureClass;
        this.featureCode = featureCode;
        this.defaultValue = defaultValue;
        this.expectedUri = expectedUri;

        location = mock(ResolvedLocation.class);
        GeoName geoname = mock(GeoName.class);
        when(geoname.getFeatureClass()).thenReturn(this.featureClass);
        when(geoname.getFeatureCode()).thenReturn(this.featureCode);
        when(location.getGeoname()).thenReturn(geoname);

        instance = new SimpleClavinOntologyMapper();
    }

    /**
     * Test of getOntologyClassUri method, of class SimpleClavinOntologyMapper.
     */
    @Test
    public void testGetOntologyClassUri() {
        assertEquals(expectedUri, instance.getOntologyClassUri(location, defaultValue));
    }
}
