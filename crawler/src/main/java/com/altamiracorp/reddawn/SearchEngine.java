package com.altamiracorp.reddawn;

import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: jprincip
 * Date: 6/5/13
 * Time: 2:56 PM
 * To change this template use File | Settings | File Templates.
 */
public abstract class SearchEngine {

    protected ArrayList<Query> queryQueue;
    protected ArrayList<Integer> maxResultQueue;

    protected Crawler crawler;

    public SearchEngine(Crawler c) {
        if(c == null) {
            System.err.println("The crawler cannot be initialized to null");
            System.exit(1);
        }
        crawler = c;
        queryQueue = new ArrayList<Query>();
        maxResultQueue = new ArrayList<Integer>();
    }

    /**
     * Adds a query and its number of results to the queue
     *
     * @param q The query to add to the queue
     * @param maxResults The maximum number of results that the method should return
     * @return Whether or not the query was successfully added
     */
    public boolean addQueryToQueue(Query q, int maxResults) {
        if(!queryQueue.add(q)) return false;
        if(maxResults < 0 || !maxResultQueue.add(maxResults)) {
            queryQueue.remove(queryQueue.size()-1);
            return false;
        }
        return true;
    }

    /**
     * Runs all of the queries entered into the engine on it
     *
     * @return List containing the result sets of links from the queries in the queue
     */
    public ArrayList<ArrayList<String>> runQueue() {
        ArrayList<ArrayList<String>> results = new ArrayList<ArrayList<String>>();

        for(int i = 0; i < queryQueue.size(); i++) {
            results.add(runQuery(queryQueue.get(i), maxResultQueue.get(i)));
        }

        return results;
    }

    /**
     * Takes a Query and runs it on the engine
     *
     * @param q The query to execute
     * @param maxResults The maximum number of results that should be returned
     * @return The links to the result pages in a list
     */
    public ArrayList<String> runQuery(Query q, int maxResults) {
        System.out.println("\n\033[1m" + queryHeader(q) + "\033[0m");
        return search(q, maxResults);
    }

    /**
     * Performs the query requested as a search, finding the links and passing them to the crawler to fetch
     *
     * @param q The Query to execute
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

	/**
	 * Returns the Engine name as a string.
	 * @return engine name
	 */
	public String getEngineName()
	{
		return this.getClass().toString();
	}

    protected String queryHeader(Query q) {
        return "Running Query" + ((q.getQueryString().length() > 0) ? " \"" + q.getQueryString() + "\"" :
                ((q.getRss().length() > 0) ? " URL: " + q.getRss() : "" )) + " on " + getEngineName() +
                ((q.getSubreddit().length() > 0) ? ", subreddit " + q.getSubreddit() : "");
    }
}
