package com.altamiracorp.reddawn.crawler;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class GoogleNewsSearchEngine extends SearchEngine {

    private String baseURL;

    public GoogleNewsSearchEngine(Crawler c) {
        super(c);
        baseURL = "http://news.google.com/news?output=rss";
    }

    @Override
    protected TreeMap<String, TreeMap<String, String>> search(Query q, int maxResults) {
        TreeMap<String, TreeMap<String, String>> results = new TreeMap<String, TreeMap<String, String>>();
        String queryUrl = createQueryUrl(q, maxResults);
        URL fullURL = null;
        try {
            fullURL = new URL(queryUrl);
        } catch (MalformedURLException e) {
            System.err.println("Malformed search URL");
            return results;
        }

        ArrayList<String> links = SearchEngine.parseRSS(fullURL, maxResults);
        for (String link : links) {
            results.put(link, null);
        }
        try {
            getCrawler().crawl(results, q);
        } catch (Exception e) {
            throw new RuntimeException("The crawler failed to crawl the " + getEngineName() + " on Query \"" +
                    q.getQueryString() + "\" result set");
        }
        return results;
    }

    protected String createQueryUrl(Query query, int maxResults) {
        TreeMap<String, String> extraParams = new TreeMap<String, String>();
        extraParams.put("num", maxResults + "");
        String queryUrl = baseURL + SearchEngine.createQueryString(processQuery(query)) +
                SearchEngine.createQueryString(extraParams);
        return queryUrl;
    }

    protected TreeMap<String, String> processQuery(Query q) {
        TreeMap<String, String> queryParams = new TreeMap<String, String>();
        queryParams.put("q", Utils.concatenate(q.getOptionalTerms(), "+") + "+" +
                Utils.concatenate(q.getRequiredTerms(), "+"));

        return queryParams;
    }

    @Override
    public String getEngineName() {
        return "Google News Search Engine";
    }

}
