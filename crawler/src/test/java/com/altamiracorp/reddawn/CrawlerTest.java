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

		ArrayList<String> links = new ArrayList<String>();
		links.add("http://www.google.com");

		crawler = new Crawler(links);
    }

	@Test
	public void testCrawl() throws Exception
	{
		assertNotNull(crawler.crawl("http://www.google.com"));
	}
}
