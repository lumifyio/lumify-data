package com.altamiracorp.reddawn;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

/**
 * Created with IntelliJ IDEA.
 * User: jprincip
 * Date: 6/7/13
 * Time: 10:11 AM
 * To change this template use File | Settings | File Templates.
 */
public class GoogleNewsSearchEngine extends SearchEngine {

    private String baseURL;

    /**
     * Builds the search engine so that it can process queries
     *
     * @param c The Crawler to employ for following the links and writing them to disk
     */
    public GoogleNewsSearchEngine(Crawler c) {
        super(c);
        baseURL = "http://news.google.com/news?output=rss";
    }

    /**
     * Performs the query requested as a search, finding the links and passing them to the crawler to fetch
     *
     * @param q The Query to execute
     * @param maxResults The number of results to return; return all if -1
     * @return List of links retrieved from the search
     */
    protected ArrayList<String> search(Query q, int maxResults) {
        String queryString = EngineFunctions.createQueryString(processQuery(q));

        // Result Links to return
        ArrayList<String> links = new ArrayList<String>();

        // Adds the result range to the query
        TreeMap<String, String> extraParams = new TreeMap<String, String>();
        extraParams.put("num", maxResults + "");

        // Creates query URL
        URL fullURL = null;
        try {
            fullURL = new URL(baseURL + queryString + EngineFunctions.createQueryString(extraParams));
        } catch (MalformedURLException e) {
            System.err.println("Malformed search URL");
            return null;
        }
		URL feedURL = null;
		links = EngineFunctions.parseRSS(fullURL, maxResults);
//		for (String googleLink : EngineFunctions.parseRSS(fullURL, maxResults))
//		{
//			try{
//				feedURL = new URL(googleLink);
//			} catch(MalformedURLException e) {
//				System.err.println("Google News provided a malformed URL. Skipping...");
//				break;
//			}
//			// Splits query parameters, identifies the redirect link, and adds it to the list of links
//			for(String param : feedURL.getQuery().split("&")) {
//				String[] kvPair = param.split("=");
//				if(kvPair[0].equals("url")) links.add(kvPair[1]);
//			}
//		}
        // Runs the results into the crawler, which processes them and writes them to the file system
        try {
            crawler.processSearchResults(links, q);
        } catch (Exception e) {
            System.err.println("The crawler failed to crawl the result set");
            e.printStackTrace();
        }

        return links;
    }

    /**
     * Takes variables from the query object and constructs key-value mappings based on the query parameters for Google News Search
     *
     * @param q Query to process
     * @return Map containing the engine-specific key-value pairs for the query
     */
    private TreeMap<String, String> processQuery(Query q) {
        TreeMap<String, String> queryParams = new TreeMap<String, String>();
        queryParams.put("q", EngineFunctions.concatenate(q.getOptionalTerms(), "+") + "+" + EngineFunctions.concatenate(q.getRequiredTerms(), "+"));

        return queryParams;
    }

	/**
	 * Returns the Engine name as a string.
	 * @return engine name
	 */
	@Override
	public String getEngineName()
	{
		return "Google News Search Engine";
	}

}
