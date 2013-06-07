package com.altamiracorp.reddawn;

import junit.framework.TestCase;
import org.junit.Before;
import org.junit.Test;

/**
 * Created with IntelliJ IDEA.
 * User: swoloszy
 * Date: 6/7/13
 * Time: 3:32 PM
 * To change this template use File | Settings | File Templates.
 */
public class RedditSearchEngineTest extends TestCase {

	RedditSearchEngine engine;

	@Before
	public void setUp() throws Exception
	{
		super.setUp();
		Crawler crawler = new Crawler();
		engine = new RedditSearchEngine(crawler);
	}

	@Test
	public void testSearch() throws Exception {
		Query query = new Query();
		query.addOptionalTerm("boston bombing");
		int maxResults = 20;
		engine.search(query, maxResults);
	}

}
