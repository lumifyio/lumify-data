package com.altamiracorp.reddawn.crawler;

import com.altamiracorp.reddawn.crawler.Crawler;
import com.altamiracorp.reddawn.crawler.Query;
import com.altamiracorp.reddawn.crawler.RSSEngine;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static junit.framework.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(JUnit4.class)
public class RSSEngineTest {

    RSSEngine engine;
    Crawler mockCrawler;

    @Before
    public void setUp() throws Exception {
        mockCrawler = mock(Crawler.class);
        engine = new RSSEngine(mockCrawler);
    }

    @Test
    public void testSearch() throws Exception {
        Query mockQuery = mock(Query.class);
        int maxResult = 5;
        when(mockQuery.getRss()).thenReturn("http://rss.cnn.com/rss/cnn_topstories.rss");
        assertEquals("A normal RSS link did not return the specified number of results (has Enginefunction and HTTP dependencies)",
                maxResult, engine.search(mockQuery, maxResult).size());
    }

    @Test
    public void testSearchEmptyURL() {
        Query mockQuery = mock(Query.class);
        when(mockQuery.getRss()).thenReturn("");
        int maxResult = 10;
        int expectedResults = 0;
        assertEquals("An empty RSS link did not return an empty result set", expectedResults,
                engine.search(mockQuery, maxResult).size());
    }
}
