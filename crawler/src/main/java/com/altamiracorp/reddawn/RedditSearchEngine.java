package com.altamiracorp.reddawn;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: swoloszy
 * Date: 6/7/13
 * Time: 3:27 PM
 * To change this template use File | Settings | File Templates.
 */
public class RedditSearchEngine extends SearchEngine{

	public RedditSearchEngine(Crawler crawler)
	{
		super(crawler);
	}

	@Override
	protected ArrayList<String> search(Query q, int maxResults) {
		ArrayList<String> results = new ArrayList<String>();
		String queryString = processQuery(q); // TO DO ADD OTHER SEARCH PARAMETERS INCLUDING MAX COUNT AKA LIMIT
		String url = "http://www.reddit.com/search.json?" + queryString;
		try
		{
			JSONObject resultsJSON = new JSONObject(EngineFunctions.searchWithGetRequest(url));
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
	 * formatted for use with an HTTP GET request.
	 * Will return the form:
	 *
	 * @param q the query to be processed
	 * @return the string representing the query
	 */
	private String processQuery(Query q)
	{
		String query = "q=";
		boolean hasAddedFirstTerm = false;
		for (String term : q.getOptionalTerms())
		{
			if (hasAddedFirstTerm)
			{
				query += "+";
			}
			else
			{
				hasAddedFirstTerm = true;
			}
			query += term.replace(" ", "+");
		}
		System.out.println(query);
		return query;
	}
}


