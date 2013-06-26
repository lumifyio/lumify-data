package com.altamiracorp.reddawn.crawler;

import com.altamiracorp.reddawn.crawler.Crawler;
import com.altamiracorp.reddawn.crawler.HttpRetrievalManager;
import com.altamiracorp.reddawn.crawler.Query;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.sql.Timestamp;
import java.util.ArrayList;

import static junit.framework.Assert.assertEquals;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

@RunWith(JUnit4.class)
public class CrawlerTest {
    Crawler crawler;
    Crawler crawlerSpy;
    HttpRetrievalManager mockedManager;

    @Before
    public void setUp() throws Exception {
        crawler = new Crawler();
        crawlerSpy = spy(crawler);
        mockedManager = mock(HttpRetrievalManager.class);
        when(crawlerSpy.createManager()).thenReturn(mockedManager);

    }

    @Test
    public void testCrawlerValid() {
        Crawler c = new Crawler(".");
    }

    @Test(expected = RuntimeException.class)
    public void testCrawlerInvalid() {
        Crawler c = new Crawler("laiefbnaeoir;wgji;byo;");
    }

    @Test
    public void testCrawlNormal() throws Exception {
        Query mockedQuery = mock(Query.class);
        ArrayList<String> urls = new ArrayList<String>();
        urls.add("a;skdfj");
        urls.add("a;al;d");
        urls.add("a;skdfasdfj");
        urls.add("aasd;skdfj");
        urls.add("a;skasdfdfj");

        int expectedNumofCalls = urls.size();


        crawlerSpy.crawl(urls, mockedQuery);
        verify(mockedManager, times(expectedNumofCalls)).addJob(anyString(), anyString(), anyString());
        verify(mockedManager).shutDownWhenFinished();
    }

    @Test
    public void testCrawlEmptyUrls() throws Exception {
        Query mockedQuery = mock(Query.class);
        ArrayList<String> urls = new ArrayList<String>();
        int expectedNumofCalls = 0;
        HttpRetrievalManager mockedManager = mock(HttpRetrievalManager.class);


        when(crawlerSpy.createManager()).thenReturn(mockedManager);

        crawlerSpy.crawl(urls, mockedQuery);
        verify(mockedManager, times(expectedNumofCalls)).addJob(anyString(), anyString(), anyString());
        verify(mockedManager).shutDownWhenFinished();
    }

    @Test
    public void testGetHeader() {
        Query mockedQuery = mock(Query.class);
        when(mockedQuery.getQueryString()).thenReturn("queryInfo");
        when(crawlerSpy.getCurrentTimestamp()).thenReturn(1000000000l);
        String result = crawlerSpy.getHeader("url", mockedQuery);
        assertEquals("<meta property=\"atc:result-url\" content=\"url\">\n" +
                    "<meta property=\"atc:retrieval-timestamp\" content=\"1000000000\">\n" +
                    "<meta property=\"atc:query-info\" content=\"queryInfo\">\n", result);
    }

}
