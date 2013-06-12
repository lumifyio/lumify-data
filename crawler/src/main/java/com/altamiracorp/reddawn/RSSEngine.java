package com.altamiracorp.reddawn;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

public class RSSEngine extends SearchEngine {
	private String url;

	public RSSEngine(Crawler c) {
		super(c);
		url = "";
	}

	@Override
	protected ArrayList<String> search(Query q, int maxResults) {
		url = q.getRss();
		ArrayList<String> links = new ArrayList<String>();
		if (url.equals("")) {
			System.err.println("No RSS URL specified");
		} else {

			URL theUrl;
			try {
				theUrl = new URL(url);
			} catch (MalformedURLException e) {
				System.err.println("Malformed search URL");
				return null;
			}
			links = EngineFunctions.parseRSS(theUrl, maxResults);

			try {
				getCrawler().crawl(links, q);
			} catch (Exception e) {
				System.err.println("The crawler failed to crawl the result set");
				e.printStackTrace();
			}
		}
		return links;
	}

	@Override
	public String getEngineName() {
		return "RSS Engine";
	}
}
