package com.altamiracorp.reddawn;

import junit.framework.TestCase;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: jprincip
 * Date: 6/11/13
 * Time: 8:56 AM
 * To change this template use File | Settings | File Templates.
 */
public class WebCrawlTest extends TestCase {

	WebCrawl driver;
	String[] argsAll = { "--query=boston marathon,boston +bombing -mit", "--provider=google,news,rss,reddit", "--directory=fakepath", "--rss=http://rss.cnn.com/rss/cnn_topstories.rss",
					"--result-count=20", "--subreddit=all,inthenews" },
			argsJustSubreddit = { "--query=boston marathon,boston +bombing -mit", "--provider=reddit", "--directory=fakepath", "--result-count=20", "--subreddit=inthenews" },
			argsNoSubreddit = { "--query=boston marathon,boston +bombing -mit", "--provider=reddit", "--directory=fakepath", "--result-count=20" },
			argsEmptySubreddit = { "--query=boston marathon,boston +bombing -mit", "--provider=google,news,rss,reddit", "--directory=fakepath", "--rss=http://rss.cnn.com/rss/cnn_topstories.rss",
					"--result-count=20", "--subreddit=" },
			argsNoRssLink = { "--query=boston marathon,boston +bombing -mit", "--provider=google,news,rss,reddit", "--directory=fakepath", "--result-count=20" },
			argsEmptyRssLink = { "--query=boston marathon,boston +bombing -mit", "--provider=google,news,rss,reddit", "--directory=fakepath", "--result-count=20", "--rss=" },
			argsMultipleRssLinks = { "--query=boston marathon", "--provider=rss", "--directory=fakepath", "--rss=http://rss.cnn.com/rss/cnn_topstories.rss,http://rss.cnn.com/rss/cnn_topstories.rss"},
			argsNegResultCount = { "--query=boston marathon,boston +bombing -mit", "--provider=google,news,rss,reddit", "--directory=fakepath", "--rss=http://rss.cnn.com/rss/cnn_topstories.rss",
					"--result-count=-20", "--subreddit=all,inthenews" },
			argsZeroResultCount = { "--query=boston marathon,boston +bombing -mit", "--provider=google,news,rss,reddit", "--directory=fakepath", "--rss=http://rss.cnn.com/rss/cnn_topstories.rss",
					"--result-count=-20", "--subreddit=all,inthenews" },
			argsNaNResultCount = { "--query=boston marathon,boston +bombing -mit", "--provider=google,news,rss,reddit", "--directory=fakepath", "--rss=http://rss.cnn.com/rss/cnn_topstories.rss",
					"--result-count=hello", "--subreddit=all,inthenews" };

	@Before
	public void setUp() {
		driver = new WebCrawl();
	}

	@Test
	public void testAddSearchQuery() {
		assertEquals("A standard search query was not properly created", "boston bombing", driver.addSearchQuery("boston bombing").getQueryString());
		assertEquals("A standard search query was not properly added to the queries list", 1, driver.getQueries().size());
		driver.clear();
		assertEquals("A search query with required and excluded terms was not properly created", "boston +bombing -marathon", driver.addSearchQuery("boston +bombing -marathon").getQueryString());
		assertEquals("A search query with required and excluded terms was not properly added", 1, driver.getQueries().size());
		driver.clear();
		assertEquals("An empty search query was not properly created", "", driver.addSearchQuery("").getQueryString());
		assertEquals("An empty search query was erroneously added to the queries list", 0, driver.getQueries().size());

	}

	@Test
	public void testAddRedditQueries() {
		driver.loadCommandLine(argsNoSubreddit);
		driver.addRedditQueries(driver.addSearchQuery("boston bombing"));
		assertEquals("A standard search query was not added to the reddit queries properly", 1, driver.getRedditQueries().size());
		assertEquals("A standard search query was not integrated into reddit properly", "", driver.getRedditQueries().get(0).getSubreddit());

		driver.clear();
		driver.loadCommandLine(argsAll);
		driver.addRedditQueries(driver.addSearchQuery("boston bombing"));
		assertEquals("A query for both the main reddit and a subreddit were not added properly", 2, driver.getRedditQueries().size());

		String subreddit = "";
		boolean hasMainReddit = false;
		for(Query q : driver.getRedditQueries()) {
			if(q.getSubreddit().length() > 0) subreddit = q.getSubreddit();
			else hasMainReddit = true;
		}

		assertTrue("A query for both the main reddit and a subreddit did not add the main reddit query properly", hasMainReddit);
		assertEquals("A query for both the main reddit and a subreddit did not add the proper subreddit query", "inthenews", subreddit);

		driver.clear();
		driver.loadCommandLine(argsJustSubreddit);
		driver.addRedditQueries(driver.addSearchQuery("boston bombing"));
		assertEquals("A query for just a subreddit did not add the subreddit properly", 1, driver.getRedditQueries().size());
		assertEquals("A query for just a subreddit did not add right subreddit name", "inthenews", driver.getRedditQueries().get(0).getSubreddit());

		driver.clear();
		driver.loadCommandLine(argsEmptySubreddit);
		driver.addRedditQueries(driver.addSearchQuery("boston bombing"));
		assertEquals("A query with an empty subreddit value did not add the right number of subreddit queries", 1, driver.getRedditQueries().size());
		assertEquals("A query with an empty subreddit value gave the added query a subreddit", "", driver.getRedditQueries().get(0).getSubreddit());
	}

	@Test
	public void testAddRSSLinks() {
		driver.loadCommandLine(argsAll);
		driver.addRSSLinks();
		assertEquals("A single RSS feed link was not properly added to the list of RSS queries", 1, driver.getRssLinks().size());

		driver.clear();
		driver.loadCommandLine(argsNoRssLink);
		driver.addRSSLinks();
		assertEquals("A query with no rss option provided erroneously added element(s) to the list of RSS links", 0, driver.getRssLinks().size());

		driver.clear();
		driver.loadCommandLine(argsEmptyRssLink);
		driver.addRSSLinks();
		assertEquals("A query with an empty rss option provided erroneously added element(s) to the list of RSS links", 0, driver.getRssLinks().size());

		driver.clear();
		driver.loadCommandLine(argsMultipleRssLinks);
		driver.addRSSLinks();
		assertEquals("A query with multiple rss links provided added the incorrect number to the list of RSS links", 2, driver.getRssLinks().size());
	}

	@Test
	public void testAddEngines() {
		WebCrawl crawlAll = new WebCrawl(argsAll);
		assertEquals("Query for all providers created an incorrect number of search engines", 4, crawlAll.getEngines().size());
		for(SearchEngine engine : crawlAll.getEngines()) {
			if(engine instanceof GoogleSearchEngine) assertEquals("Google search engine received the incorrect query list", 2, engine.getQueryQueue().size());
			else if(engine instanceof GoogleNewsSearchEngine) assertEquals("Google news search engine received the incorrect query list", 2, engine.getQueryQueue().size());
			else if(engine instanceof RedditSearchEngine) assertEquals("Reddit search engine received the incorrect query list", 4, engine.getQueryQueue().size());
			else if(engine instanceof RSSEngine) assertEquals("RSS search engine received the incorrect query list", 1, engine.getQueryQueue().size());
		}
	}

	@Test
	public void testSetResultCount() {
		driver.loadCommandLine(argsAll);
		driver.setResultCount();
		assertEquals("Setting standard result count returned the incorrect result count", 20, driver.getResults());

		driver.clear();
		driver.loadCommandLine(argsNegResultCount);
		driver.setResultCount();
		assertEquals("Setting negative result count did not return default result count", WebCrawl.DEFAULT_RESULT_COUNT, driver.getResults());

		driver.clear();
		driver.loadCommandLine(argsZeroResultCount);
		driver.setResultCount();
		assertEquals("Setting zero result count did not return default result count", WebCrawl.DEFAULT_RESULT_COUNT, driver.getResults());

		driver.clear();
		driver.loadCommandLine(argsNaNResultCount);
		driver.setResultCount();
		assertEquals("Setting non-number result count did not return default result count", WebCrawl.DEFAULT_RESULT_COUNT, driver.getResults());
	}

	@Test
	public void testParseQuery() {
		Map<String, ArrayList<String>> params = WebCrawl.parseQuery("boston bombing");
		assertEquals("A standard query did not return the right number of terms", 2, params.get("optional").size());
		assertEquals("A standard query added erroneous required and/or excluded terms", 0, params.get("excluded").size() + params.get("required").size());

		params = WebCrawl.parseQuery("boston +bombing -mit");
		assertEquals("A query with required, optional, and excluded terms did not add optional terms to the map properly", 1, params.get("optional").size());
		assertEquals("A query with required, optional, and excluded terms did not add required terms to the map properly", 1, params.get("required").size());
		assertEquals("A query with required, optional, and excluded terms did not add excluded terms to the map properly", 1, params.get("excluded").size());

		params = WebCrawl.parseQuery("");
		assertEquals("An empty query did not add anyterms to the query", 0, params.get("optional").size() + params.get("required").size() + params.get("excluded").size());
	}
}
