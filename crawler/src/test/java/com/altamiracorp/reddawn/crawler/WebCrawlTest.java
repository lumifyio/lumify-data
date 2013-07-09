package com.altamiracorp.reddawn.crawler;

import org.apache.commons.cli.CommandLine;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static org.mockito.Mockito.*;

@RunWith(JUnit4.class)
public class WebCrawlTest {

    WebCrawl driverSpy;
    CommandLine mockCL;
    Query mockQuery;

    @Before
    public void setUp() {
        driverSpy = spy(new WebCrawl());
        mockCL = mock(CommandLine.class);
        driverSpy.setCrawler(mock(Crawler.class));
        driverSpy.setCommandLine(mockCL);
        mockQuery = spy(new Query());
        when(mockQuery.getOptionalTerms()).thenReturn(new ArrayList<String>(Arrays.asList("boston", "bombing")));
    }

    @Test
    public void testAddSearchQuery() {
        assertEquals("A standard search query was not properly created", "boston bombing", driverSpy.addSearchQuery("boston bombing").getQueryString());
        assertEquals("A standard search query was not properly added to the queries list", 1, driverSpy.getQueries().size());
    }

    @Test
    public void testAddSearchQueryComplex() {
        assertEquals("A search query with required and excluded terms was not properly created", "boston +bombing -marathon", driverSpy.addSearchQuery("boston +bombing -marathon").getQueryString());
        assertEquals("A search query with required and excluded terms was not properly added", 1, driverSpy.getQueries().size());
    }

    @Test
    public void testAddSearchQueryEmpty() {
        assertEquals("An empty search query was not properly created", "", driverSpy.addSearchQuery("").getQueryString());
        assertEquals("An empty search query was erroneously added to the queries list", 0, driverSpy.getQueries().size());
    }

    @Test
    public void testAddRedditQueriesMain() {
        when(mockCL.getOptionValue("subreddit")).thenReturn(null);
        driverSpy.addRedditQueries(mockQuery);
        assertEquals("A standard search query was not added to the reddit queries properly", 1, driverSpy.getRedditQueries().size());
        assertEquals("A standard search query was not integrated into reddit properly", "", driverSpy.getRedditQueries().get(0).getSubreddit());
    }

    @Test
    public void testAddRedditQueriesMainAndSubreddit() {
        when(mockCL.getOptionValue("subreddit")).thenReturn("all,inthenews");
        driverSpy.addRedditQueries(mockQuery);
        assertEquals("A query for both the main reddit and a subreddit were not added properly", 2, driverSpy.getRedditQueries().size());

        String subreddit = "";
        boolean hasMainReddit = false;
        for (Query q : driverSpy.getRedditQueries()) {
            if (q.getSubreddit().length() > 0) {
                subreddit = q.getSubreddit();
            } else {
                hasMainReddit = true;
            }
        }

        assertTrue("A query for both the main reddit and a subreddit did not add the main reddit query properly", hasMainReddit);
        assertEquals("A query for both the main reddit and a subreddit did not add the proper subreddit query", "inthenews", subreddit);
    }

    @Test
    public void testAddRedditQueriesSubreddit() {
        when(mockCL.getOptionValue("subreddit")).thenReturn("inthenews");
        driverSpy.addRedditQueries(mockQuery);
        assertEquals("A query for just a subreddit did not add the subreddit properly", 1, driverSpy.getRedditQueries().size());
        assertEquals("A query for just a subreddit did not add right subreddit name", "inthenews", driverSpy.getRedditQueries().get(0).getSubreddit());
    }

    @Test
    public void testAddRedditQueriesEmptySubreddit() {
        when(mockCL.getOptionValue("subreddit")).thenReturn("");
        driverSpy.addRedditQueries(mockQuery);
        assertEquals("A query with an empty subreddit value did not add the right number of subreddit queries", 1, driverSpy.getRedditQueries().size());
        assertEquals("A query with an empty subreddit value gave the added query a subreddit", "", driverSpy.getRedditQueries().get(0).getSubreddit());
    }

    @Test
    public void testAddRSSLinks() {
        when(mockCL.getOptionValue("rss")).thenReturn("http://rss.cnn.com/rss/cnn_topstories.rss");
        driverSpy.addRSSLinks();
        assertEquals("A single RSS feed link was not properly added to the list of RSS queries", 1, driverSpy.getRssLinks().size());
    }

    @Test
    public void testAddRSSLinksNone() {
        when(mockCL.getOptionValue("rss")).thenReturn(null);
        driverSpy.addRSSLinks();
        assertEquals("A query with no rss option provided erroneously added element(s) to the list of RSS links", 0, driverSpy.getRssLinks().size());
    }

    @Test
    public void testAddRSSLinksEmpty() {
        when(mockCL.getOptionValue("rss")).thenReturn("");
        driverSpy.addRSSLinks();
        assertEquals("A query with an empty rss option provided erroneously added element(s) to the list of RSS links", 0, driverSpy.getRssLinks().size());
    }

    @Test
    public void testAddRSSLinksMultiple() {
        when(mockCL.getOptionValue("rss")).thenReturn("http://rss.cnn.com/rss/cnn_topstories.rss,http://rss.cnn.com/rss/cnn_topstories.rss");
        driverSpy.addRSSLinks();
        assertEquals("A query with multiple rss links provided added the incorrect number to the list of RSS links", 2, driverSpy.getRssLinks().size());
    }

    @Test
    public void testAddEngines() {
        when(mockCL.getOptionValue("provider")).thenReturn("google,news,reddit,rss");
        when(driverSpy.getQueries()).thenReturn(new ArrayList<Query>(Arrays.asList(new Query(), new Query())));
        when(driverSpy.getRedditQueries()).thenReturn(new ArrayList<Query>(Arrays.asList(new Query(), new Query(),
                new Query(), new Query())));
        when(driverSpy.getRssLinks()).thenReturn(new ArrayList<Query>(Arrays.asList(new Query())));

        driverSpy.addEngines();
        assertEquals("Query for all providers created an incorrect number of search engines", 4, driverSpy.getEngines().size());
    }

    @Test
    public void testSetResultCount() {
        when(mockCL.getOptionValue("result-count")).thenReturn("20");
        driverSpy.setResultCount();
        assertEquals("Setting standard result count returned the incorrect result count", 20, driverSpy.getResults());
    }

    @Test
    public void testSetResultCountNegative() {
        when(mockCL.getOptionValue("result-count")).thenReturn("-20");
        driverSpy.setResultCount();
        assertEquals("Setting negative result count did not return default result count", WebCrawl.DEFAULT_RESULT_COUNT, driverSpy.getResults());
    }

    @Test
    public void testSetResultCountZero() {
        when(mockCL.getOptionValue("result-count")).thenReturn("0");
        driverSpy.setResultCount();
        assertEquals("Setting zero result count did not return default result count", WebCrawl.DEFAULT_RESULT_COUNT, driverSpy.getResults());
    }

    @Test
    public void testSetResultCountNaN() {
        when(mockCL.getOptionValue("result-count")).thenReturn("hello");
        driverSpy.setResultCount();
        assertEquals("Setting non-number result count did not return default result count", WebCrawl.DEFAULT_RESULT_COUNT, driverSpy.getResults());
    }

    @Test
    public void testParseQueryOptional() {
        Map<String, ArrayList<String>> params = WebCrawl.parseQuery("boston bombing");
        assertEquals("A standard query did not return the right number of terms", 2, params.get("optional").size());
        assertEquals("A standard query added erroneous required and/or excluded terms", 0, params.get("excluded").size() + params.get("required").size());
    }

    @Test
    public void testParseQueryOptionalRequiredExcluded() {
        Map<String, ArrayList<String>> params = WebCrawl.parseQuery("boston +bombing -mit");
        assertEquals("A query with required, optional, and excluded terms did not add optional terms to the map properly", 1, params.get("optional").size());
        assertEquals("A query with required, optional, and excluded terms did not add required terms to the map properly", 1, params.get("required").size());
        assertEquals("A query with required, optional, and excluded terms did not add excluded terms to the map properly", 1, params.get("excluded").size());
    }

    @Test
    public void testParseQueryEmpty() {
        Map<String, ArrayList<String>> params = WebCrawl.parseQuery("");
        assertEquals("An empty query did not add anyterms to the query", 0, params.get("optional").size() + params.get("required").size() + params.get("excluded").size());
    }
}
