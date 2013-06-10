package com.altamiracorp.reddawn;

import junit.framework.TestCase;
import org.junit.After;
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
		System.out.println();
		System.out.println();

		System.out.println();

	}

	@Test
	public void testGeneralSearch() throws Exception
	{
		Query query = new Query();
		query.addOptionalTerm("boston bombing");
		int maxResults = 3;
		engine.search(query, maxResults);
	}


	@Test
	public void testSubredditSearch() throws Exception
	{
		Query query = new Query();
		query.addOptionalTerm("boston bombing");
		int maxResults = 3;
		query.setSubreddit("inthenews");
		engine.search(query, maxResults);
	}

	@Test
	public void testMainPageCrawl() throws Exception
	{
		Query query = new Query();
		int maxResults = 3;
		engine.search(query, maxResults);
	}

	@Test
	public void testSubredditMainPageCrawl() throws Exception
	{
		Query query = new Query();
		query.setSubreddit("inthenews");
		int maxResults = 3;
		engine.search(query, maxResults);
	}
}