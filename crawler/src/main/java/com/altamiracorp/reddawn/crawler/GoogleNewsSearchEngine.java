package com.altamiracorp.reddawn.crawler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class GoogleNewsSearchEngine extends SearchEngine {
    private static final Logger LOGGER = LoggerFactory.getLogger(GoogleNewsSearchEngine.class);

    private String baseURL;

    public GoogleNewsSearchEngine(Crawler c) {
        super(c);
        baseURL = "http://news.google.com/news?output=rss";
    }

    @Override
    protected List<String> search(Query q, int maxResults) {
        ArrayList<String> results = new ArrayList<String>();
        String queryUrl = createQueryUrl(q, maxResults);
        URL fullURL = null;
        try {
            fullURL = new URL(queryUrl);
        } catch (MalformedURLException e) {
            LOGGER.error("Malformed search URL");
            return results;
        }

        ArrayList<String> links = SearchEngine.parseRSS(fullURL, maxResults);
        for (String link : links) {
            results.add(link);
        }
        try {
            getCrawler().crawl(results, q);
        } catch (Exception e) {
            LOGGER.error("The crawler failed to crawl the " + getEngineName() + " on Query \"" +
                    q.getQueryString() + "\" result set");
            e.printStackTrace();
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
