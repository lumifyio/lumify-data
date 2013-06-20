package com.altamiracorp.reddawn.crawler;

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

        links = SearchEngine.parseRSS(fullURL, maxResults);
        try {
            getCrawler().crawl(links, q);
        } catch (Exception e) {
            throw new RuntimeException("The crawler failed to crawl the " + getEngineName() + " on Query \"" +
                    q.getQueryString() + "\" result set");
        }
        return links;
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
