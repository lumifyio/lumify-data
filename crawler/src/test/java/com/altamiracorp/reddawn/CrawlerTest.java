package com.altamiracorp.reddawn;

import junit.framework.TestCase;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: swoloszy
 * Date: 6/6/13
 * Time: 9:24 AM
 * To change this template use File | Settings | File Templates.
 */
public class CrawlerTest extends TestCase{
   	Crawler crawler;

    @Before
    public void setUp() throws Exception {
        super.setUp();
		crawler = new Crawler();
    }

	@Test
	public void testProcessSearchResults() throws Exception
	{
		ArrayList<String> links = new ArrayList<String>();
		links.add("http://www.google.com");
		links.add("http://www.reddit.com/");
		links.add("http://www.cnn.com/");
		links.add("http://abcnews.go.com/");
		links.add("http://news.yahoo.com/");
		links.add("http://www.fairfaxtimes.com/");

		Query query = new Query();
		query.addOptionalTerm("search");
		crawler.run(links, query);
	}
	@Test
	public void testRedirectLinkProcessSearchResults() throws Exception
	{
		ArrayList<String> links = new ArrayList<String>();
		links.add("http://davidsimon.com/we-are-shocked-shocked/");
		Query query = new Query();
		query.addOptionalTerm("search");
		crawler.run(links, query);
	}

	@Test
	public void testBadLink() throws Exception
	{
		ArrayList<String> links = new ArrayList<String>();
		links.add("http://www.reddit.com/a;sldkfj");
		Query query = new Query();
		query.addOptionalTerm("search");
		crawler.run(links, query);
	}
}
