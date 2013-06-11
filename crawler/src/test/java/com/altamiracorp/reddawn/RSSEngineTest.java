package com.altamiracorp.reddawn;

import junit.framework.TestCase;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.ArrayList;

import static org.mockito.Mockito.*;

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
		Query query = new Query();
		query.addOptionalTerm("boston bombing");
		query.setRSSFeed("http://rss.cnn.com/rss/cnn_world.rss");
		int maxResults = 3;
		engine.search(query, maxResults);
		verify(mockCrawler).processSearchResults(any(ArrayList.class), any(Query.class));
	}
}
