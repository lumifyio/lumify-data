package com.altamiracorp.reddawn;
import org.json.JSONException;

import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: jprincip
 * Date: 6/5/13
 * Time: 2:56 PM
 * To change this template use File | Settings | File Templates.
 */
public interface SearchEngine {
    /**
     * Adds the specified query to a queue of queries to execute on the selected search engine
     *
     * @param q The query to add to the queue
     * @param maxResults The maximum number of results that the method should return
     * @return Whether or not the query was successfully added to the queue
     */
    public boolean addQueryToQueue(Query q, int maxResults);

    /**
     * Runs every item in the queue through the search engine
     *
     * @return ArrayList holding the list of URLs retrieved from each search in the queue
     */
    public ArrayList<ArrayList<String>> runQueue();

    /**
     * Runs the query passed into the method on the search engine
     *
     * @param q The query to execute
     * @param maxResults The maximum number of results that should be returned
     * @return List of URLs retrieved from the result table
     */
    public ArrayList<String> runQuery(Query q, int maxResults);
}
