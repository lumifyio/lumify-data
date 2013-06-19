package com.altamiracorp.reddawn.crawler;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

public class RSSEngine extends SearchEngine {
    private String url = "";

    public RSSEngine(Crawler c) {
        super(c);
    }

    @Override
    protected ArrayList<String> search(Query q, int maxResults) {
        url = q.getRss();
        ArrayList<String> links = new ArrayList<String>();
        if (url.equals("")) {
            System.err.println("No RSS URL specified");
            return links;
        } else {
            URL theUrl;
            try {
                theUrl = new URL(url);
            } catch (MalformedURLException e) {
                System.err.println("Malformed search URL");
                return null;
            }
            links = SearchEngine.parseRSS(theUrl, maxResults);

            try {
                getCrawler().crawl(links, q);
            } catch (Exception e) {
                throw new RuntimeException("The crawler failed to crawl the " + getEngineName() + " on link \"" +
                        q.getRss() + "\" result set");
            }
        }
        return links;
    }

    @Override
    public String getEngineName() {
        return "RSS Engine";
    }
}
