package com.altamiracorp.reddawn;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class RedditSearchEngine extends SearchEngine {

	public RedditSearchEngine(Crawler crawler) {
		super(crawler);
	}

	@Override
	protected ArrayList<String> search(Query q, int maxResults) {
		ArrayList<String> results = new ArrayList<String>();
		String queryUrl = createQueryString(q, maxResults);

		try {
			JSONObject resultsJSON = new JSONObject(EngineFunctions.getWebpage(queryUrl));
			JSONArray childrenEntries = resultsJSON.getJSONObject("data").getJSONArray("children");
			for (int i = 0; i < maxResults; i++) {
				results.add(childrenEntries.getJSONObject(i).getJSONObject("data").getString("url"));
			}
		} catch (JSONException e) {
			System.err.println("Results not successfully processed as JSON");
			return results;
		}
		try {
			getCrawler().crawl(results, q);
		} catch (Exception e) {
			System.err.println("The crawler failed to crawl the result set");
			e.printStackTrace();
		}

		return results;
	}

	protected String createQueryString(Query q, int maxResults) {
		String url = "http://www.reddit.com/";
		ArrayList<String> terms = new ArrayList<String>();

		for (String s : q.getOptionalTerms()) {
			terms.add(s);
		}
		for (String s : q.getRequiredTerms()) {
			terms.add(s);
		}

		if (terms.size() == 0) {
			if (!q.getSubreddit().equals("")) {
				url += "r/" + q.getSubreddit() + "/";
			}
			url += ".json?limit=" + maxResults;
		} else {
			if (!q.getSubreddit().equals("")) {
				url += "r/" + q.getSubreddit() + "/";
			}
			url += "search.json?limit=" + maxResults + "&q=" + EngineFunctions.concatenate(terms, "+");
			if (!q.getSubreddit().equals("")) {
				url += "&restrict_sr=true";
			}
		}

		return url;
	}

	@Override
	public String getEngineName() {
		return "Reddit Search Engine";
	}
}