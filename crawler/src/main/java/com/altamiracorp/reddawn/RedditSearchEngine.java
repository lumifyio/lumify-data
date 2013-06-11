package com.altamiracorp.reddawn;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.TreeMap;

/**
 * Created with IntelliJ IDEA.
 * User: swoloszy
 * Date: 6/7/13
 * Time: 3:27 PM
 * To change this template use File | Settings | File Templates.
 */
public class RedditSearchEngine extends SearchEngine {

	public RedditSearchEngine(Crawler crawler) {
		super(crawler);
	}

	/**
	 * Performs the query requested as a search, finding the links and passing them to the crawler to fetch.
	 *
	 * @param q          The Query to execute
	 * @param maxResults The number of results to return
	 * @return
	 */
	@Override
	protected ArrayList<String> search(Query q, int maxResults) {
		ArrayList<String> results = new ArrayList<String>();
		String queryUrl = createQueryString(q, maxResults); // TO DO ADD OTHER SEARCH PARAMETERS INCLUDING MAX COUNT AKA LIMIT

		try {
			JSONObject resultsJSON = new JSONObject(EngineFunctions.searchWithGetRequest(queryUrl));
			JSONArray childrenEntries = resultsJSON.getJSONObject("data").getJSONArray("children");
			for (int i = 0; i < maxResults; i++) {
				results.add(childrenEntries.getJSONObject(i).getJSONObject("data").getString("url"));
			}
		} catch (JSONException e) {
			System.err.println("Results not successfully processed as JSON");
			return null;
		}
		try {
			getCrawler().processSearchResults(results, q);
		} catch (Exception e) {
			System.err.println("The crawler failed to crawl the result set");
			e.printStackTrace();
		}

		return results;
	}

	/**
	 * Creates a string containing the query parameters
	 * formatted as the search URL to be run.
	 *
	 * @param q the query to be processed
	 * @return the string representing the query
	 */
	private String createQueryString(Query q, int maxResults) {
//		System.out.println("Optional: " + q.getOptionalTerms().toString() + "\nRequired: " + q.getRequiredTerms().toString() + "\nExcluded: " + q.getExcludedTerms().toString());
		String url = "http://www.reddit.com/";
		TreeMap<String, String> extraParams = new TreeMap<String, String>();
		ArrayList<String> terms = new ArrayList<String>();
		for (String s : q.getOptionalTerms()) {
			terms.add(s);
		}
		for (String s : q.getRequiredTerms()) {
			terms.add(s);
		}

		//new
		if (terms.size() == 0) {
			if (!q.getSubreddit().equals("")) // subreddit specified
			{
				url += "r/" + q.getSubreddit() + "/";
			}
			url += ".json?limit=" + maxResults;
		} else {
			if (!q.getSubreddit().equals("")) // subreddit specified
			{
				url += "r/" + q.getSubreddit() + "/";
			}
			url += "search.json?limit=" + maxResults + "&q=" + EngineFunctions.concatenate(terms, "+");
			if (!q.getSubreddit().equals("")) // subreddit specified
			{
				url += "&restruct_sr=true";
			}
		}

		System.out.println(url);

		return url;
	}


	/**
	 * Returns the Engine name as a string.
	 *
	 * @return engine name
	 */
	@Override
	public String getEngineName() {
		return "Reddit Search Engine";
	}
}


