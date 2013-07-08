package com.altamiracorp.reddawn.crawler;

import junit.framework.TestCase;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;

import static org.mockito.Mockito.*;

public class SearchEngineTest extends TestCase {
    SearchEngine engine;
    Query mockedQuery1;
    Query mockedQuery2;
    Query mockedQuery3;
    SearchEngine engineSpy;

    @Before
    public void setUp() throws Exception {
        super.setUp();
        Crawler mockedCrawler = mock(Crawler.class);
        engine = new GoogleSearchEngine(mockedCrawler);

        mockedQuery1 = mock(Query.class);
        ArrayList<String> optionalTerms = new ArrayList<String>();
        optionalTerms.add("boston bombing");
        optionalTerms.add("2013");
        when(mockedQuery1.getOptionalTerms()).thenReturn(optionalTerms);
        when(mockedQuery1.getQueryString()).thenReturn("normalQueryString");
        when(mockedQuery1.getSubreddit()).thenReturn("normalSubreddit");

        mockedQuery2 = mock(Query.class);
        when(mockedQuery2.getQueryString()).thenReturn("");
        when(mockedQuery2.getRss()).thenReturn("normalRss");
        when(mockedQuery2.getSubreddit()).thenReturn("");

        mockedQuery3 = mock(Query.class);
        when(mockedQuery3.getQueryString()).thenReturn("");
        when(mockedQuery3.getRss()).thenReturn("");
        when(mockedQuery3.getSubreddit()).thenReturn("");

        engineSpy = spy(engine);
        when(engineSpy.getEngineName()).thenReturn("Search Engine");
    }

    @Test
    public void testAddQueryToQueueNormal() throws Exception {
        int maxResults = 7;
        boolean addedSuccessfully = engine.addQueryToQueue(mockedQuery1, maxResults);
        assertTrue("Properly formatted queries are not added correctly", addedSuccessfully);
        assertEquals("Properly formatted queries are causing the queues to become unbalanced",
                engine.getMaxResultQueue().size(), engine.getQueryQueue().size());
    }

    @Test
    public void testAddQueryToQueueInvalidMaxResultCount() {
        int maxResults = -1;
        boolean second = engine.addQueryToQueue(mockedQuery1, maxResults);
        assertFalse("Negative max result counts are added when they shouldn't be", second);
        assertEquals("Negative max result counts are causing the queues to become unbalanced",
                engine.getMaxResultQueue().size(), engine.getQueryQueue().size());
    }

    @Test
    public void testQueryHeaderSearchTermsAndSubredditQuery() {
        String result = engineSpy.queryHeader(mockedQuery1);
        assertEquals("Running Query \"normalQueryString\" on Search Engine, subreddit: normalSubreddit", result);
    }

    @Test
    public void testQueryHeaderRssQuery() {
        String result = engineSpy.queryHeader(mockedQuery2);
        assertEquals("Running Query URL: normalRss on Search Engine", result);
    }

    @Test
    public void testQueryHeaderEmpty() {
        String result = engineSpy.queryHeader(mockedQuery3);
        assertEquals("Running Query on Search Engine", result);
    }
}