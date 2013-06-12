package com.altamiracorp.reddawn;

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
	public void testCrawlNormal() throws Exception{
		Query mockedQuery = mock(Query.class);
		ArrayList<String> urls = new ArrayList<String>();
		urls.add("a;skdfj");
		urls.add("a;al;d");
		urls.add("a;skdfasdfj");
		urls.add("aasd;skdfj");
		urls.add("a;skasdfdfj");

		int expectedNumofCalls = 5;


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
		when(mockedQuery.getQueryInfo()).thenReturn("queryInfo");
		Timestamp mockedTimestamp = mock(Timestamp.class);
		when(mockedTimestamp.toString()).thenReturn("timestamp");
		when(crawlerSpy.getCurrentTimestamp()).thenReturn(mockedTimestamp);
		String result = crawlerSpy.getHeader("url", mockedQuery);
		assertEquals("contentSource: url\ntimeOfRetrieval: timestamp\nqueryInfo: queryInfo\n", result);
	}

}
