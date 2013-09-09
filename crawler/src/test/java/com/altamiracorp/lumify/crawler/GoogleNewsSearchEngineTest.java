package com.altamiracorp.lumify.crawler;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.ArrayList;
import java.util.TreeMap;

import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(JUnit4.class)
public class GoogleNewsSearchEngineTest {
    private GoogleNewsSearchEngine engine;
    private Query mockQuery;

    private Crawler mockCrawler;

    @Before
    public void setUp() {
        mockQuery = mock(Query.class);
        mockCrawler = mock(Crawler.class);
        engine = new GoogleNewsSearchEngine(mockCrawler);
    }

    // TODO this is an integration test and needs to be refactored to a unit test
//    @Test
//    public void testSearch() throws Exception {
//        mockQuery = mock(Query.class);
//        int maxResult = 5;
//        assertEquals("A normal search Google News query did not return the specified number of results (has Enginefunction and HTTP dependencies)",
//                maxResult, engine.search(mockQuery, maxResult).size());
//    }

    @Test
    public void testSearchEmpty() {
        mockQuery = mock(Query.class);
        int maxResult = 10;
        int expectedResults = 10;
        assertEquals(expectedResults,
                engine.search(mockQuery, maxResult).size());
    }

    @Test
    public void testCreateQueryUrl() {
        ArrayList<String> optionalTerms2 = new ArrayList<String>();
        optionalTerms2.add("boston bombing");
        when(mockQuery.getOptionalTerms()).thenReturn(optionalTerms2);
        ArrayList<String> requiredTerms2 = new ArrayList<String>();
        requiredTerms2.add("us");
        when(mockQuery.getRequiredTerms()).thenReturn(requiredTerms2);
        int maxResults = 15;
        String baseURL = "http://news.google.com/news?output=rss";
        String expected = baseURL + "&q=boston+bombing+us" + "&num=15";
        String result = engine.createQueryUrl(mockQuery, maxResults);
        assertEquals(expected, result);
    }

    @Test
    public void testProcessQuery() {
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
        TreeMap<String, String> results = engine.processQuery(mockQuery);
        assertTrue(results.containsKey("q"));
        assertTrue(results.get("q").contentEquals("boston+bombing+united+states+bombing+boston+marathon"));
    }

    @Test
    public void testGetEngineName() {
        assertEquals("Google News Search Engine", engine.getEngineName());
    }
}
