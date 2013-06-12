package com.altamiracorp.reddawn;

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
		when(mockQuery.getRss()).thenReturn("http://rss.cnn.com/rss/cnn_topstories.rss");
		assertEquals("A normal RSS link did not return the specified number of results (has Enginefunction and HTTP dependencies)",
				5, engine.search(mockQuery, 5).size());
	}

	@Test
	public void testSearchEmptyURL() {
		Query mockQuery = mock(Query.class);
		when(mockQuery.getRss()).thenReturn("");
		assertEquals("An empty RSS link did not return an empty result set", 0, engine.search(mockQuery, 10).size());
	}
}
