package com.altamiracorp.reddawn.crawler;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

public class RSSEngine extends SearchEngine {
    private String url = "";

    public RSSEngine(Crawler c) {
        super(c);
    }

    @Override
    protected TreeMap<String, TreeMap<String, String>> search(Query q, int maxResults) {
        url = q.getRss();
        TreeMap<String, TreeMap<String, String>> results = new TreeMap<String, TreeMap<String, String>>();
        if (url.equals("")) {
            System.err.println("No RSS URL specified");
            return results;
        } else {
            URL theUrl;
            try {
                theUrl = new URL(url);
            } catch (MalformedURLException e) {
                System.err.println("Malformed search URL");
                return null;
            }
            ArrayList<String> links = SearchEngine.parseRSS(theUrl, maxResults);
            for (String link : links) {
                results.put(link, null);
            }

            try {
                getCrawler().crawl(results, q);
            } catch (Exception e) {
                throw new RuntimeException("The crawler failed to crawl the " + getEngineName() + " on link \"" +
                        q.getRss() + "\" result set");
            }
        }
        return results;
    }

    @Override
    public String getEngineName() {
        return "RSS Engine";
    }
}
