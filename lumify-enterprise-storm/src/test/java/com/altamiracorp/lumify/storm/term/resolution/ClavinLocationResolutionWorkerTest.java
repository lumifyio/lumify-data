/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.altamiracorp.lumify.storm.term.resolution;


import static com.altamiracorp.lumify.core.model.properties.EntityLumifyProperties.GEO_LOCATION;
import static com.altamiracorp.lumify.core.model.properties.EntityLumifyProperties.GEO_LOCATION_DESCRIPTION;
import static com.altamiracorp.lumify.storm.term.resolution.ClavinLocationResolutionWorker.*;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import com.altamiracorp.lumify.core.config.Configuration;
import com.altamiracorp.lumify.core.ingest.term.extraction.TermExtractionResult;
import com.altamiracorp.lumify.core.ingest.term.extraction.TermMention;
import com.altamiracorp.lumify.core.model.ontology.Concept;
import com.altamiracorp.lumify.core.model.ontology.OntologyRepository;
import com.altamiracorp.lumify.core.user.User;
import com.altamiracorp.securegraph.type.GeoPoint;
import com.bericotech.clavin.extractor.LocationOccurrence;
import com.bericotech.clavin.gazetteer.CountryCode;
import com.bericotech.clavin.gazetteer.GeoName;
import com.bericotech.clavin.resolver.LuceneLocationResolver;
import com.bericotech.clavin.resolver.ResolvedLocation;
import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ClavinLocationResolutionWorker.class, Configuration.class, File.class})
public class ClavinLocationResolutionWorkerTest {
    private static final String TEST_IDX_PATH = "/test/index";

    private static Map<String, String> genDisabledMap() {
        return genConfigMap("true", null, null, null, null);
    }

    private static Map<String, String> genIndexOnlyMap(final String idxPath) {
        return genConfigMap(null, idxPath, null, null, null);
    }

    private static Map<String, String> genIntPropMap(final String maxHitDepth, final String maxContextWindow) {
        return genConfigMap(null, TEST_IDX_PATH, maxHitDepth, maxContextWindow, null);
    }

    private static Map<String, String> genFuzzyMap(final String fuzzy) {
        return genConfigMap(null, TEST_IDX_PATH, null, null, fuzzy);
    }

    private static Map<String, String> genConfigMap(final String disabled, final String idxPath, final String maxHitDepth,
                                                    final String maxContextWindow, final String fuzzy) {
        Map<String, String> config = new HashMap<String, String>();
        config.put(CLAVIN_DISABLED, disabled);
        config.put(CLAVIN_INDEX_DIRECTORY, idxPath);
        config.put(CLAVIN_MAX_HIT_DEPTH, maxHitDepth);
        config.put(CLAVIN_MAX_CONTEXT_WINDOW, maxContextWindow);
        config.put(CLAVIN_USE_FUZZY_MATCHING, fuzzy);
        return config;
    }

    @Mock
    private User user;

    @Mock
    private ClavinOntologyMapper mapper;

    @Mock
    private TermExtractionResult termExtractionResult;

    @Mock
    private LuceneLocationResolver luceneLocationResolver;

    @Mock
    private OntologyRepository ontologyRepo;
    @Mock
    private Concept locationConcept;

    @Mock
    private File indexDirectory;

    @Mock
    private ResolvedLocation resolvedLocation1;
    @Mock
    private ResolvedLocation resolvedLocation2;
    @Mock
    private GeoName geoName1;
    @Mock
    private GeoName geoName2;

    private TermMention locationMention1;
    private TermMention locationMention2;
    private TermMention nonLocationMention;
    private LocationOccurrence locOccur1;
    private LocationOccurrence locOccur2;
    private ClavinLocationResolutionWorker instance;
    private TermMention resolvedMention1;
    private TermMention resolvedMention2;

    private static final String ALDIE = "Aldie";
    private static final String WASHINGTON = "Washington";
    private static final String TEST_PROCESS = "testProc";
    private static final String TEST_USER = "testuser";
    private static final String ONT_LOCATION = "location";
    private static final String ONT_USERNAME = "username";
    private static final String ONT_CITY = "city";
    private static final String ONT_CAPITAL = "capital";
    private static final String ALDIE_RESOLVED = "Aldie, VA";
    private static final String WASHINGTON_RESOLVED = "Washington, D.C.";
    private static final CountryCode US_COUNTRY_CODE = CountryCode.US;
    private static final String ALDIE_ADMIN1_CODE = "VA";
    private static final String WASHINGTON_ADMIN1_CODE = "DC";
    private static final String RESOLVED_ALDIE_SIGN = String.format("%s (%s, %s)", ALDIE_RESOLVED, US_COUNTRY_CODE, ALDIE_ADMIN1_CODE);
    private static final String RESOLVED_WASHINGTON_SIGN = String.format("%s (%s, %s)", WASHINGTON_RESOLVED, US_COUNTRY_CODE, WASHINGTON_ADMIN1_CODE);
    private static final double ALDIE_LAT = 38.9756d;
    private static final double ALDIE_LONG = -77.6414d;
    private static final double WASHINGTON_LAT = 38.8951d;
    private static final double WASHINGTON_LONG = -77.0367d;
    private static final GeoPoint ALDIE_POINT = new GeoPoint(ALDIE_LAT, ALDIE_LONG);
    private static final GeoPoint WASHINGTON_POINT = new GeoPoint(WASHINGTON_LAT, WASHINGTON_LONG);
    private static final Map<String, Object> ALDIE_PROPS;
    private static final Map<String, Object> WASHINGTON_PROPS;

    static {
        Map<String, Object> aldieProps = new HashMap<String, Object>();
        aldieProps.put(GEO_LOCATION.getKey(), GEO_LOCATION.wrap(ALDIE_POINT));
        aldieProps.put(GEO_LOCATION_DESCRIPTION.getKey(), GEO_LOCATION_DESCRIPTION.wrap(ALDIE));
        ALDIE_PROPS = Collections.unmodifiableMap(aldieProps);

        Map<String, Object> dcProps = new HashMap<String, Object>();
        dcProps.put(GEO_LOCATION.getKey(), GEO_LOCATION.wrap(WASHINGTON_POINT));
        dcProps.put(GEO_LOCATION_DESCRIPTION.getKey(), GEO_LOCATION_DESCRIPTION.wrap(WASHINGTON));
        WASHINGTON_PROPS = Collections.unmodifiableMap(dcProps);
    }

    @Before
    public void setup() throws Exception {
        PowerMockito.whenNew(File.class).withArguments(TEST_IDX_PATH).thenReturn(indexDirectory);
        PowerMockito.whenNew(LuceneLocationResolver.class).withArguments(eq(indexDirectory), anyInt(), anyInt()).
                thenReturn(luceneLocationResolver);

        when(ontologyRepo.getConceptByName(ONT_LOCATION)).thenReturn(locationConcept);
        when(ontologyRepo.getAllLeafNodesByConcept(locationConcept)).thenReturn(Arrays.asList(locationConcept));
        when(locationConcept.getTitle()).thenReturn(ONT_LOCATION);

        instance = new ClavinLocationResolutionWorker();
        instance.setOntologyMapper(mapper);
        instance.setOntologyRepository(ontologyRepo);

        locationMention1 = new TermMention.Builder()
                .start(10)
                .end(10 + ALDIE.length())
                .sign(ALDIE)
                .ontologyClassUri(ONT_LOCATION)
                .resolved(false)
                .useExisting(false)
                .process(TEST_PROCESS)
                .build();

        locationMention2 = new TermMention.Builder()
                .start(27)
                .end(27 + WASHINGTON.length())
                .sign(WASHINGTON)
                .ontologyClassUri(ONT_LOCATION)
                .resolved(false)
                .useExisting(false)
                .process(TEST_PROCESS)
                .build();

        nonLocationMention = new TermMention.Builder()
                .start(20)
                .end(20 + TEST_USER.length())
                .sign(TEST_USER)
                .ontologyClassUri(ONT_USERNAME)
                .resolved(false)
                .useExisting(false)
                .process(TEST_PROCESS)
                .build();

        locOccur1 = new LocationOccurrence(locationMention1.getSign(), locationMention1.getStart());
        locOccur2 = new LocationOccurrence(locationMention2.getSign(), locationMention2.getStart());

        when(resolvedLocation1.getLocation()).thenReturn(locOccur1);
        when(resolvedLocation1.getGeoname()).thenReturn(geoName1);
        when(geoName1.getName()).thenReturn(ALDIE_RESOLVED);
        when(geoName1.getPrimaryCountryCode()).thenReturn(US_COUNTRY_CODE);
        when(geoName1.getAdmin1Code()).thenReturn(ALDIE_ADMIN1_CODE);
        when(geoName1.getLatitude()).thenReturn(ALDIE_LAT);
        when(geoName1.getLongitude()).thenReturn(ALDIE_LONG);

        when(resolvedLocation2.getLocation()).thenReturn(locOccur2);
        when(resolvedLocation2.getGeoname()).thenReturn(geoName2);
        when(geoName2.getName()).thenReturn(WASHINGTON_RESOLVED);
        when(geoName2.getPrimaryCountryCode()).thenReturn(US_COUNTRY_CODE);
        when(geoName2.getAdmin1Code()).thenReturn(WASHINGTON_ADMIN1_CODE);
        when(geoName2.getLatitude()).thenReturn(WASHINGTON_LAT);
        when(geoName2.getLongitude()).thenReturn(WASHINGTON_LONG);

        when(mapper.getOntologyClassUri(resolvedLocation1, ONT_LOCATION)).thenReturn(ONT_CITY);
        when(mapper.getOntologyClassUri(resolvedLocation2, ONT_LOCATION)).thenReturn(ONT_CAPITAL);

        resolvedMention1 = new TermMention.Builder()
                .start(locationMention1.getStart())
                .end(locationMention1.getEnd())
                .sign(RESOLVED_ALDIE_SIGN)
                .ontologyClassUri(ONT_CITY)
                .properties(ALDIE_PROPS)
                .resolved(true)
                .useExisting(true)
                .build();

        resolvedMention2 = new TermMention.Builder()
                .start(locationMention2.getStart())
                .end(locationMention2.getEnd())
                .sign(RESOLVED_WASHINGTON_SIGN)
                .ontologyClassUri(ONT_CAPITAL)
                .properties(WASHINGTON_PROPS)
                .resolved(true)
                .useExisting(true)
                .build();
    }

    @Test
    public void testDisabledTrue() throws Exception {
        instance.prepare(genDisabledMap(), user);
        TermExtractionResult processed = instance.resolveTerms(termExtractionResult);

        assertEquals(processed, termExtractionResult);
        verify(mapper, never()).getOntologyClassUri(any(ResolvedLocation.class), anyString());
    }

    @Test
    public void testPrepare_NullIndexPath() throws Exception {
        doIndexErrorTest(null);
    }

    @Test
    public void testPrepare_EmptyIndexPath() throws Exception {
        doIndexErrorTest("");
    }

    @Test
    public void testPrepare_WhitespaceIndexPath() throws Exception {
        doIndexErrorTest("\n \t\t \n");
    }

    @Test
    public void testPrepare_NonexistentIndexPath() throws Exception {
        when(indexDirectory.exists()).thenReturn(false);
        doIndexErrorTest(TEST_IDX_PATH);
    }

    @Test
    public void testPrepare_NonDirectoryIndexPath() throws Exception {
        when(indexDirectory.exists()).thenReturn(true);
        when(indexDirectory.isDirectory()).thenReturn(false);
        doIndexErrorTest(TEST_IDX_PATH);
    }

    private void doIndexErrorTest(final String idxPath) throws Exception {
        Exception err = null;
        try {
            instance.prepare(genIndexOnlyMap(idxPath), user);
        } catch (Exception ex) {
            err = ex;
        }
        assertNotNull(err);
        assertThat(err, instanceOf(IllegalArgumentException.class));
        PowerMockito.verifyNew(LuceneLocationResolver.class, never()).withArguments(any(File.class), anyInt(), anyInt());
    }

    @Test
    public void testPrepare_IndexPathOnly() throws Exception {
        doPrepareTest(genIndexOnlyMap(TEST_IDX_PATH));
    }

    @Test
    public void testPrepare_IntParamsNegative() throws Exception {
        doPrepareTest(genIntPropMap("-42", "-3"));
    }

    @Test
    public void testPrepare_IntParamsZero() throws Exception {
        doPrepareTest(genIntPropMap("0", "0"));
    }

    @Test
    public void testPrepare_IntParamsOne() throws Exception {
        doPrepareTest(1, 1);
    }

    @Test
    public void testPrepare_IntParamsGtOne() throws Exception {
        doPrepareTest(27, 42);
    }

    @Test
    public void testPrepare_FuzzyTrueLower() throws Exception {
        doFuzzyPrepareTest("true", true);
    }

    @Test
    public void testPrepare_FuzzyTrueUpper() throws Exception {
        doFuzzyPrepareTest("TRUE", true);
    }

    @Test
    public void testPrepare_FuzzyTrueMixed() throws Exception {
        doFuzzyPrepareTest("tRuE", true);
    }

    @Test
    public void testPrepare_FuzzyTrueUntrimmed() throws Exception {
        doFuzzyPrepareTest("   \tTruE\n\t\t ", true);
    }

    @Test
    public void testPrepare_FuzzyFalseLower() throws Exception {
        doFuzzyPrepareTest("false", false);
    }

    @Test
    public void testPrepare_FuzzyFalseUpper() throws Exception {
        doFuzzyPrepareTest("FALSE", false);
    }

    @Test
    public void testPrepare_FuzzyFalseMixed() throws Exception {
        doFuzzyPrepareTest("FaLsE", false);
    }

    @Test
    public void testPrepare_FuzzyFalseUntrimmed() throws Exception {
        doFuzzyPrepareTest("   \t false\n\t\t ", false);
    }

    @Test
    public void testPrepare_FuzzyEmptyStr() throws Exception {
        doNonBooleanFuzzyPrepareTest("");
    }

    @Test
    public void testPrepare_FuzzyWhitespaceStr() throws Exception {
        doNonBooleanFuzzyPrepareTest("\n \t\t \n");
    }

    @Test
    public void testPrepare_FuzzyNonBooleanStr() throws Exception {
        doNonBooleanFuzzyPrepareTest("Not a Boolean");
    }

    private void doPrepareTest(final Map<String, String> config) throws Exception {
        doPrepareTest(config, ClavinLocationResolutionWorker.DEFAULT_MAX_HIT_DEPTH, ClavinLocationResolutionWorker.DEFAULT_MAX_CONTENT_WINDOW, ClavinLocationResolutionWorker.DEFAULT_FUZZY_MATCHING);
    }

    private void doPrepareTest(final int expectedHitDepth, final int expectedContextWindow)
            throws Exception {
        doPrepareTest(genIntPropMap("" + expectedHitDepth, "" + expectedContextWindow), expectedHitDepth, expectedContextWindow,
                DEFAULT_FUZZY_MATCHING);
    }

    private void doPrepareTest(final Map<String, String> config, final boolean expectedFuzzy) throws Exception {
        doPrepareTest(config, DEFAULT_MAX_HIT_DEPTH, ClavinLocationResolutionWorker.DEFAULT_MAX_CONTENT_WINDOW, expectedFuzzy);
    }

    private void doPrepareTest(final Map<String, String> config, final int expectedHitDepth, final int expectedContextWindow,
                               final boolean expectedFuzzy) throws Exception {
        when(indexDirectory.exists()).thenReturn(true);
        when(indexDirectory.isDirectory()).thenReturn(true);

        instance.prepare(config, user);

        PowerMockito.verifyNew(LuceneLocationResolver.class).withArguments(indexDirectory, expectedHitDepth, expectedContextWindow);
        assertEquals(expectedFuzzy, Whitebox.getInternalState(instance, "fuzzy"));
    }

    private void doFuzzyPrepareTest(final String fuzzyStr, final boolean expectedFuzzy) throws Exception {
        doPrepareTest(genFuzzyMap(fuzzyStr), expectedFuzzy);
    }

    private void doNonBooleanFuzzyPrepareTest(final String fuzzyStr) throws Exception {
        doPrepareTest(genFuzzyMap(fuzzyStr));
    }

    private void prepareWithDefaults() throws Exception {
        when(indexDirectory.exists()).thenReturn(true);
        when(indexDirectory.isDirectory()).thenReturn(true);
        instance.prepare(genIndexOnlyMap(TEST_IDX_PATH), user);
    }

    @Test
    public void testResolveTerms_NullResult() throws Exception {
        prepareWithDefaults();
        TermExtractionResult result = instance.resolveTerms(null);
        assertNull(result);
    }

    @Test
    public void testResolveTerms_NoLocations() throws Exception {
        prepareWithDefaults();
        TermExtractionResult result = instance.resolveTerms(termExtractionResult);
        assertSame(termExtractionResult, result);
        verify(termExtractionResult, never()).replace(any(TermMention.class), any(TermMention.class));
    }

    @Test
    public void testResolveTerms_NoResolvedLocations() throws Exception {
        List<TermMention> mentions = Arrays.asList(locationMention1, locationMention2, nonLocationMention);
        List<LocationOccurrence> occurrences = Arrays.asList(locOccur1, locOccur2);
        when(termExtractionResult.getTermMentions()).thenReturn(mentions);
        when(luceneLocationResolver.resolveLocations(occurrences, DEFAULT_FUZZY_MATCHING)).thenReturn(Collections.EMPTY_LIST);

        prepareWithDefaults();
        TermExtractionResult result = instance.resolveTerms(termExtractionResult);
        assertSame(termExtractionResult, result);
        verify(termExtractionResult, never()).replace(any(TermMention.class), any(TermMention.class));
    }

    @Test
    public void testResolveTerms_SomeResolvedLocations() throws Exception {
        List<TermMention> mentions = Arrays.asList(locationMention1, nonLocationMention, locationMention2);
        List<LocationOccurrence> occurrences = Arrays.asList(locOccur1, locOccur2);
        when(termExtractionResult.getTermMentions()).thenReturn(mentions);
        when(luceneLocationResolver.resolveLocations(occurrences, DEFAULT_FUZZY_MATCHING)).thenReturn(Arrays.asList(resolvedLocation1));

        prepareWithDefaults();
        TermExtractionResult result = instance.resolveTerms(termExtractionResult);
        assertSame(termExtractionResult, result);

//        verify(termExtractionResult).replace(locationMention1, resolvedMention1);
        verify(termExtractionResult, never()).replace(eq(locationMention2), any(TermMention.class));
        verify(termExtractionResult, never()).replace(eq(nonLocationMention), any(TermMention.class));
    }

    @Test
    public void testResolveTerms_AllResolvedLocations() throws Exception {
        List<TermMention> mentions = Arrays.asList(locationMention1, nonLocationMention, locationMention2);
        List<LocationOccurrence> occurrences = Arrays.asList(locOccur1, locOccur2);
        when(termExtractionResult.getTermMentions()).thenReturn(mentions);
        when(luceneLocationResolver.resolveLocations(occurrences, DEFAULT_FUZZY_MATCHING)).
                thenReturn(Arrays.asList(resolvedLocation1, resolvedLocation2));

        prepareWithDefaults();
        TermExtractionResult result = instance.resolveTerms(termExtractionResult);
        assertSame(termExtractionResult, result);

//        verify(termExtractionResult).replace(locationMention1, resolvedMention1);
//        verify(termExtractionResult).replace(locationMention2, resolvedMention2);
        verify(termExtractionResult, never()).replace(eq(nonLocationMention), any(TermMention.class));
    }
}
