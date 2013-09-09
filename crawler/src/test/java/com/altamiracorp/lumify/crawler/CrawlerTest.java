package com.altamiracorp.lumify.crawler;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.ArrayList;

import static junit.framework.Assert.assertEquals;
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
        crawlerSpy.crawl(urls, mockedQuery);
    }

    @Test
    public void testCrawlEmptyUrls() throws Exception {
        Query mockedQuery = mock(Query.class);
        ArrayList<String> urls = new ArrayList<String>();
        int expectedNumofCalls = 0;
        crawlerSpy.crawl(urls, mockedQuery);
    }

}
