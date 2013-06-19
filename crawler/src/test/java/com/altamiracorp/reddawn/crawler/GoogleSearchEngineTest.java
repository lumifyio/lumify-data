package com.altamiracorp.reddawn.crawler;

import com.altamiracorp.reddawn.crawler.Crawler;
import com.altamiracorp.reddawn.crawler.GoogleSearchEngine;
import com.altamiracorp.reddawn.crawler.Query;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.Matchers;

import java.util.ArrayList;
import java.util.TreeMap;

import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(JUnit4.class)
public class GoogleSearchEngineTest {
    private GoogleSearchEngine engine;
    private Query mockQuery;
    private Crawler mockCrawler;
    final int MAX_RESULTS = 55;

    @Before
    public void setUp() throws Exception {
        mockQuery = mock(Query.class);
        mockCrawler = mock(Crawler.class);
        engine = new GoogleSearchEngine(mockCrawler);
        ArrayList<String> optionalTerms = new ArrayList<String>();
        optionalTerms.add("boston bombing");
        optionalTerms.add("united states");
        ArrayList<String> requiredTerms = new ArrayList<String>();
        requiredTerms.add("bombing");
        requiredTerms.add("boston marathon");
        ArrayList<String> excludedTerms = new ArrayList<String>();
        excludedTerms.add("2012");
        excludedTerms.add("excluded term");
        when(mockQuery.getRequiredTerms()).thenReturn(requiredTerms);
        when(mockQuery.getOptionalTerms()).thenReturn(optionalTerms);
        when(mockQuery.getExcludedTerms()).thenReturn(excludedTerms);
    }

    @Test
    public void testSearchValid() throws Exception {
        GoogleSearchEngine engineSpy = spy(engine);
        doReturn("https://www.googleapis.com/customsearch/v1?" +
                "key=AIzaSyB4H5oZoRFCVsNoYUNI6nCNAMAusD1GpDY&cx=012249192867828703671:vknw0znfgfa&alt=json&q=bombing")
                .when(engineSpy).getQueryString(any(Query.class));
        engineSpy.search(mock(Query.class), 10);
        verify(mockCrawler).crawl(Matchers.<ArrayList<String>>any(), any(Query.class));
    }

    @Test
    public void testSearchInvalid() throws Exception {
        GoogleSearchEngine engineSpy = spy(engine);
        doReturn("http://www.google.com").when(engineSpy).getQueryString(any(Query.class));
        ArrayList<String> results = engineSpy.search(mockQuery, 10);
        verify(mockCrawler, times(0)).crawl(Matchers.<ArrayList<String>>any(), any(Query.class));
        assertEquals("An invalid JSON response did not return an empty link set", 0, results.size());
    }

    @Test
    public void testGetResultRangeFirstSearch() {
        int searchCount = 0;
        String result = engine.getResultRange(searchCount, MAX_RESULTS);
        assertEquals("&num=10&start=1", result);
    }

    @Test
    public void testGetResultRangeSecondSearch() {
        int searchCount = 2;
        String result = engine.getResultRange(searchCount, MAX_RESULTS);
        assertEquals("&num=10&start=21", result);
    }

    @Test
    public void testGetResultRangeLast() {
        int searchCount = 5;
        String result = engine.getResultRange(searchCount, MAX_RESULTS);
        assertEquals("&num=5&start=51", result);
    }

    @Test
    public void testProcessQuery() {
        TreeMap<String, String> results = engine.processQuery(mockQuery);
        assertTrue(results.containsKey("q"));
        assertTrue(results.get("q").contentEquals("boston+bombing+united+states"));
        assertTrue(results.containsKey("exactTerms"));
        assertTrue(results.get("exactTerms").contentEquals("bombing+boston+marathon"));
        assertTrue(results.containsKey("excludeTerms"));
        assertTrue(results.get("excludeTerms").contentEquals("2012+excluded+term"));
    }

    @Test
    public void testGetEngineName() {
        assertEquals("Google Search Engine", engine.getEngineName());
    }
}
