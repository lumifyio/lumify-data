package com.altamiracorp.reddawn;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.TreeMap;

public class GoogleNewsSearchEngine extends SearchEngine {

	private String baseURL;

	public GoogleNewsSearchEngine(Crawler c) {
		super(c);
		baseURL = "http://news.google.com/news?output=rss";
	}

	@Override
	protected ArrayList<String> search(Query q, int maxResults) {
		ArrayList<String> links = new ArrayList<String>();
		String queryUrl = createQueryUrl(q, maxResults);
		URL fullURL = null;
		try {
			fullURL = new URL(queryUrl);
		} catch (MalformedURLException e) {
			System.err.println("Malformed search URL");
			return links;
		}

		links = EngineFunctions.parseRSS(fullURL, maxResults);
		try {
			getCrawler().crawl(links, q);
		} catch (Exception e) {
			System.err.println("The crawler failed to crawl the result set");
			e.printStackTrace();
		}
		return links;
	}

	protected String createQueryUrl(Query query, int maxResults) {
		TreeMap<String, String> extraParams = new TreeMap<String, String>();
		extraParams.put("num", maxResults + "");
		String queryUrl =  baseURL + EngineFunctions.createQueryString(processQuery(query)) + EngineFunctions.createQueryString(extraParams);
		return queryUrl;
	}

	protected TreeMap<String, String> processQuery(Query q) {
		TreeMap<String, String> queryParams = new TreeMap<String, String>();
		queryParams.put("q", EngineFunctions.concatenate(q.getOptionalTerms(), "+") + "+" + EngineFunctions.concatenate(q.getRequiredTerms(), "+"));

		return queryParams;
	}

	@Override
	public String getEngineName() {
		return "Google News Search Engine";
	}

}
