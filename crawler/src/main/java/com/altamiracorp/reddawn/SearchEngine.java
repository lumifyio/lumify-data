package com.altamiracorp.reddawn;

import java.util.ArrayList;

public abstract class SearchEngine {

	private ArrayList<Query> queryQueue;
	private ArrayList<Integer> maxResultQueue;

	private Crawler crawler;

	public SearchEngine(Crawler c) {
		if (c == null) {
			System.err.println("The crawler cannot be initialized to null");
			System.exit(1);
		}
		crawler = c;
		queryQueue = new ArrayList<Query>();
		maxResultQueue = new ArrayList<Integer>();
	}

	public boolean addQueryToQueue(Query q, int maxResults) {
		if (!queryQueue.add(q)) return false;
		if (maxResults < 0 || !maxResultQueue.add(maxResults)) {
			queryQueue.remove(queryQueue.size() - 1);
			return false;
		}
		return true;
	}

	public ArrayList<ArrayList<String>> runQueue() {
		ArrayList<ArrayList<String>> results = new ArrayList<ArrayList<String>>();
		for (int i = 0; i < queryQueue.size(); i++) {
			results.add(runQuery(queryQueue.get(i), maxResultQueue.get(i)));
		}
		return results;
	}

	public ArrayList<String> runQuery(Query q, int maxResults) {
		System.out.println("\n\033[1m" + queryHeader(q) + "\033[0m");
		return search(q, maxResults);
	}

	/**
	 * Performs the query requested as a search,
	 * finding the links and passing them to the crawler to fetch
	 *
	 * @param q          The Query to execute
	 * @param maxResults The number of results to return
	 * @return List of links retrieved from the search
	 */
	protected abstract ArrayList<String> search(Query q, int maxResults);

	public Crawler getCrawler() {
		return crawler;
	}

	public ArrayList<Query> getQueryQueue() {
		return queryQueue;
	}

	public ArrayList<Integer> getMaxResultQueue() {
		return maxResultQueue;
	}

	public String getEngineName() {
		return this.getClass().toString();
	}

	public String queryHeader(Query q) {
		String queryString = "";
		if (q.getQueryString().length() > 0) {
			queryString += " \"" + q.getQueryString() + "\"";
		} else if (q.getRss().length() > 0) {
			queryString += " URL: " + q.getRss();
		}
		String subreddit = "";
		if((q.getSubreddit().length() > 0) ) {
	 		subreddit += ", subreddit: " + q.getSubreddit();
		}
		return "Running Query" + queryString + " on " + getEngineName() + subreddit;
	}
}
