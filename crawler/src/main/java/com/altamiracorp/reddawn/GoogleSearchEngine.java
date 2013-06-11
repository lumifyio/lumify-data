package com.altamiracorp.reddawn;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: jprincip
 * Date: 6/5/13
 * Time: 3:07 PM
 * To change this template use File | Settings | File Templates.
 */
public class GoogleSearchEngine extends SearchEngine {

    private String baseURL;

    public GoogleSearchEngine(Crawler c) {
        super(c);
        baseURL = "https://www.googleapis.com/customsearch/v1?key=AIzaSyB4H5oZoRFCVsNoYUNI6nCNAMAusD1GpDY" +
                "&cx=012249192867828703671:vknw0znfgfa" +
                "&alt=json";
    }

    public GoogleSearchEngine(Crawler c, String apiKey, String searchEngineID) {
        super(c);
        baseURL = "https://www.googleapis.com/customsearch/v1?key=" + apiKey +
                "&cx=" + searchEngineID +
                "&alt=json";
    }

    /**
     * Runs query on search engine
     *
     * @param q Query object to execute on the engine
     * @param maxResults The maximum number of results to display
     * @return ArrayList of links from the result set
     */
    protected ArrayList<String> search(Query q, int maxResults) {
        String queryString = EngineFunctions.createQueryString(processQuery(q));

        // Result Links to return
        ArrayList<String> links = new ArrayList<String>();

        // Loops on the number of searches it needs to crawl to get the desired number of results
        for(int searchCount = 0; searchCount*10 < maxResults; searchCount++) {
            // Adds the result ranges to the query
            TreeMap<String, String> extraParams = new TreeMap<String, String>();
            extraParams.put("num", (maxResults - searchCount * 10 < 10 ? maxResults - searchCount * 10 : 10) + "");
            extraParams.put("start", searchCount * 10 + 1 + "");
			String queryURL = baseURL + queryString + EngineFunctions.createQueryString(extraParams);

            // Get response from page and put it in a JSON object (return type should be JSON)
            try {
                JSONObject response = new JSONObject(EngineFunctions.getWebpage(queryURL));
                JSONArray results = response.getJSONArray("items");

                for(int i = 0; i < results.length(); i++) {
                    JSONObject entry = results.getJSONObject(i);
                    links.add(entry.getString("link"));
                }
            } catch(JSONException e) {
                System.err.println("The response from the server is not valid JSON");
                return null;
            }
        }

        try {
            getCrawler().crawl(links, q);
        } catch (Exception e) {
            System.err.println("The crawler failed to crawl the result set");
            e.printStackTrace();
        }

        return links;
    }

    /**
     * Takes variables from the query object and constructs key-value mappings based on the Custom Search API parameters
     *
     * @param q Query to process
     * @return Map containing the engine-specific key-value pairs for the query
     */
    private TreeMap<String, String> processQuery(Query q) {
        TreeMap<String, String> queryParams = new TreeMap<String, String>();

        // Generates query strings for search terms
        String required = EngineFunctions.concatenate(q.getRequiredTerms(), "+");
        String excluded = EngineFunctions.concatenate(q.getExcludedTerms(), "+");
        String optional = EngineFunctions.concatenate(q.getOptionalTerms(), "+");

        // Adds query strings to the map if they aren't empty
        if(required.length() > 0) queryParams.put("exactTerms", required);
        if(excluded.length() > 0) queryParams.put("excludeTerms", excluded);
        queryParams.put("q", optional);

        return queryParams;
    }

	/**
	 * Returns the Engine name as a string.
	 * @return engine name
	 */
	@Override
	public String getEngineName()
	{
		return "Google Search Engine";
	}
}
