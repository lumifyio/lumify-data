package com.altamiracorp.reddawn;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.Matchers;

import java.util.ArrayList;
import java.util.Arrays;

import static junit.framework.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.*;

@RunWith(JUnit4.class)
public class RedditSearchEngineTest {

	RedditSearchEngine engine;
	Crawler mockCrawler;

	@Before
	public void setUp() throws Exception {
		mockCrawler = mock(Crawler.class);
		engine = new RedditSearchEngine(mockCrawler);
	}

	@Test
	public void testSearchValid() throws Exception {
		RedditSearchEngine engineSpy = spy(engine);
		doReturn("http://www.reddit.com/.json?limit=20").when(engineSpy).createQueryString(any(Query.class), anyInt());
		engineSpy.search(mock(Query.class), 10);
		verify(mockCrawler).crawl(Matchers.<ArrayList<String>>any(), any(Query.class));
	}

	@Test
	public void testSearchInvalid() throws Exception {
		RedditSearchEngine engineSpy = spy(engine);
		doReturn("http://www.google.com").when(engineSpy).createQueryString(any(Query.class), anyInt());
		ArrayList<String> results = engineSpy.search(mock(Query.class), 10);
		verify(mockCrawler, times(0)).crawl(Matchers.<ArrayList<String>>any(), any(Query.class));
		assertEquals("An invalid JSON response did not return an empty link set", 0, results.size());
	}

	@Test
	public void testCreateQueryStringMain() {
		Query mockQuery = mock(Query.class);
		when(mockQuery.getOptionalTerms()).thenReturn(new ArrayList<String>());
		when(mockQuery.getRequiredTerms()).thenReturn(new ArrayList<String>());
		when(mockQuery.getSubreddit()).thenReturn("");
		assertEquals("http://www.reddit.com/.json?limit=20", engine.createQueryString(mockQuery, 20));
	}

	@Test
	public void testCreateQueryStringMainSearch() {
		Query mockQuery = mock(Query.class);
		when(mockQuery.getOptionalTerms()).thenReturn(new ArrayList<String>(Arrays.asList("term1", "term2")));
		when(mockQuery.getRequiredTerms()).thenReturn(new ArrayList<String>(Arrays.asList("requiredTerm1", "requiredTerm2")));
		when(mockQuery.getSubreddit()).thenReturn("");
		assertEquals("http://www.reddit.com/search.json?limit=20&q=term1+term2+requiredTerm1+requiredTerm2",
				engine.createQueryString(mockQuery, 20));
	}

	@Test
	public void testCreateQueryStringSubreddit() {
		Query mockQuery = mock(Query.class);
		when(mockQuery.getOptionalTerms()).thenReturn(new ArrayList<String>());
		when(mockQuery.getRequiredTerms()).thenReturn(new ArrayList<String>());
		when(mockQuery.getSubreddit()).thenReturn("inthenews");
		assertEquals("http://www.reddit.com/r/inthenews/.json?limit=20", engine.createQueryString(mockQuery, 20));
	}

	@Test
	public void testCreateQueryStringSubredditSearch() {
		Query mockQuery = mock(Query.class);
		when(mockQuery.getOptionalTerms()).thenReturn(new ArrayList<String>(Arrays.asList("term1", "term2")));
		when(mockQuery.getRequiredTerms()).thenReturn(new ArrayList<String>(Arrays.asList("requiredTerm1", "requiredTerm2")));
		when(mockQuery.getSubreddit()).thenReturn("inthenews");
		assertEquals("http://www.reddit.com/r/inthenews/search.json?limit=20&q=term1+term2+requiredTerm1+requiredTerm2&restrict_sr=true",
				engine.createQueryString(mockQuery, 20));
	}
}