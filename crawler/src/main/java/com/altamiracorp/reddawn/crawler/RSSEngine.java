package com.altamiracorp.reddawn.crawler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

public class RSSEngine extends SearchEngine {
    private static final Logger LOGGER = LoggerFactory.getLogger(RSSEngine.class);

    private String url = "";

    public RSSEngine(Crawler c) {
        super(c);
    }

    @Override
    protected ArrayList<String> search(Query q, int maxResults) {
        url = q.getRss();
        ArrayList<String> results = new ArrayList<String>();
        if (url.equals("")) {
           LOGGER.error("No RSS URL specified");
            return results;
        } else {
            URL theUrl;
            try {
                theUrl = new URL(url);
            } catch (MalformedURLException e) {
                LOGGER.error("Malformed search URL");
                return null;
            }
            ArrayList<String> links = SearchEngine.parseRSS(theUrl, maxResults);
            for (String link : links) {
                results.add(link);
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
