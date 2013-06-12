package com.altamiracorp.reddawn;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.ArrayList;
import java.util.Arrays;

@RunWith(JUnit4.class)
public class CrawlerTest {
	Crawler crawler;

	@Before
	public void setUp() throws Exception {
		crawler = new Crawler();
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
	public void testCreateHttpConnectionThreadsNormal() {
		ArrayList<String> links = new ArrayList<String>();
		links.add("http://google.com");

	}

	// Old tests are below. Delete them eventually.
	// Problems with testing: no real input or output, and only one public method.
	// Suggestions on how to test these?
	// Should we make createHttpConnectionThread a protected (instead of private) method?

	@Test
	public void testProcessSearchResults() throws Exception {
		ArrayList<String> links = new ArrayList<String>();
		links.add("http://www.google.com");
		links.add("http://www.reddit.com/");
		links.add("http://www.cnn.com/");
		links.add("http://abcnews.go.com/");
		links.add("http://news.yahoo.com/");
		links.add("http://www.fairfaxtimes.com/");

		Query query = new Query();
		query.addOptionalTerm("search");
		crawler.crawl(links, query);
	}

	@Test
	public void testRedirectLinkProcessSearchResults() throws Exception {
		ArrayList<String> links = new ArrayList<String>();
		links.add("http://davidsimon.com/we-are-shocked-shocked/");
		Query query = new Query();
		query.addOptionalTerm("search");
		crawler.crawl(links, query);
	}

	@Test
	public void testBadLink() throws Exception {
		ArrayList<String> links = new ArrayList<String>();
		links.add("http://www.reddit.com/a;sldkfj");
		Query query = new Query();
		query.addOptionalTerm("search");
		crawler.crawl(links, query);
	}
}
