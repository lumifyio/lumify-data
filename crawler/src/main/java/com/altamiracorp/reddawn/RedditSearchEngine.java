package com.altamiracorp.reddawn;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: swoloszy
 * Date: 6/7/13
 * Time: 3:27 PM
 * To change this template use File | Settings | File Templates.
 */
public class RedditSearchEngine extends SearchEngine{

	private String subreddit;

	public RedditSearchEngine(Crawler crawler)
	{
		super(crawler);
		subreddit = "";
	}

	/**
	 * Performs the query requested as a search, finding the links and passing them to the crawler to fetch.
	 * @param q The Query to execute
	 * @param maxResults The number of results to return
	 * @return
	 */
	@Override
	protected ArrayList<String> search(Query q, int maxResults) {
		ArrayList<String> results = new ArrayList<String>();
		String queryUrl = createQueryString(q, maxResults); // TO DO ADD OTHER SEARCH PARAMETERS INCLUDING MAX COUNT AKA LIMIT

		try
		{
			JSONObject resultsJSON = new JSONObject(EngineFunctions.searchWithGetRequest(queryUrl));
			JSONArray childrenEntries = resultsJSON.getJSONObject("data").getJSONArray("children");
			for (int i = 0; i < maxResults; i++)
			{
				results.add(childrenEntries.getJSONObject(i).getJSONObject("data").getString("url"));
			}
		}
		catch (JSONException e)
		{
			System.err.println("Results not successfully processed as JSON");
			return null;
		}
		try {
			crawler.processSearchResults(results, q);
		} catch (Exception e) {
			System.err.println("The crawler failed to crawl the result set");
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Creates a string containing the query parameters
	 * formatted as the search URL to be run.
	 *
	 * @param q the query to be processed
	 * @return the string representing the query
	 */
	private String createQueryString(Query q, int maxResults)
	{
		String url = "";
		if (subreddit.equals(""))
		{
			 url =  "http://www.reddit.com/search.json?"
			 		+ "q=" + EngineFunctions.concatenate(q.getOptionalTerms(), "+")
			 		+ "&limit=" + maxResults;
		}
		else
		{
			url = "http://www.reddit.com/r/" + EngineFunctions.toSlug(subreddit) + "/search.json?"
					+ "q=" + EngineFunctions.concatenate(q.getOptionalTerms(), "+")
					+ "&limit=" + maxResults
					+ "&restrict_sr=true";
		}
		System.out.println(url);
		return url;
	}

	/**
	 * Specifies a subreddit to limit the search to.
	 * @param subreddit_
	 */
	public void setSubreddit(String subreddit_)
	{
		subreddit = subreddit_;
	}

	/**
	 * Resets the engine to search all of reddit
	 * with no restriction to a subreddit.
	 */
	public void clearSubreddit()
	{
		subreddit = "";
	}
}


