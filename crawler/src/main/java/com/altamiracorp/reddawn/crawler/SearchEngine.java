package com.altamiracorp.reddawn.crawler;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public abstract class SearchEngine {

    private ArrayList<Query> queryQueue;
    private ArrayList<Integer> maxResultQueue;

    private Crawler crawler;

    public SearchEngine(Crawler c) {
        if (c == null) {
            throw new RuntimeException("The crawler cannot be initialized to null");
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
        if ((q.getSubreddit().length() > 0)) {
            subreddit += ", subreddit: " + q.getSubreddit();
        }
        return "Running Query" + queryString + " on " + getEngineName() + subreddit;
    }

    public static String createQueryString(Map<String, String> params) {
        String ret = "";

        // Adds GET variable to the query for each parameter in the map
        for (Map.Entry<String, String> entry : params.entrySet()) {
            ret += "&" + entry.getKey() + "=" + entry.getValue();
        }

        return ret;
    }

    /**
     * This method is intended for use by the Search Engines
     * in order to get the results of a search without following the links returned.
     * It uses URLConnection and does not follow redirects.
     *
     * @param queryURL
     * @return
     */
    public static String getWebpage(String queryURL) {
        // Creates query URL
        URL fullURL = null;
        try {
            fullURL = new URL(queryURL);
        } catch (MalformedURLException e) {
            System.err.println("Malformed search URL");
            return null;
        }

        StringBuilder builder = new StringBuilder();
        try {
            URLConnection connection = fullURL.openConnection();
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line);
            }
        } catch (IOException e) {
            System.err.println("The http connection failed");
            return null;
        }
        return builder.toString();
    }

    public static ArrayList<String> parseRSS(URL url, int maxResults) {
        ArrayList<String> links = new ArrayList<String>();
        SAXReader saxReader = new SAXReader();
        Document xml;
        try {
            xml = saxReader.read(url);
        } catch (DocumentException e) {
            System.err.println("The specified URL (" + url + ") does not produce a valid document");
            return null;
        }
        List items = xml.getRootElement().element("channel").elements("item");
        int loopLimit = maxResults;
        if (maxResults > items.size()) {
            loopLimit = items.size();
        }
        for (int i = 0; i < loopLimit; i++) {
            Element link = ((Element) items.get(i)).element("link");
            links.add(link.getStringValue());
        }
        return links;
    }
}
