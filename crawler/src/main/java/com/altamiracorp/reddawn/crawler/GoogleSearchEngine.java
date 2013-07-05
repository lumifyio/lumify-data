package com.altamiracorp.reddawn.crawler;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.TreeMap;

public class GoogleSearchEngine extends SearchEngine {

    private String baseURL;
    final int RESULTS_PER_SEARCH = 10;

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

    protected ArrayList<String> search(Query query, int maxResults) {
        String queryString = getQueryString(query);
        ArrayList<String> links = new ArrayList<String>();

        for (int searchCount = 0; searchCount * RESULTS_PER_SEARCH < maxResults; searchCount++) {
            String queryURL = queryString + getResultRange(searchCount, maxResults);
            try {
                JSONObject response = new JSONObject(SearchEngine.getWebpage(queryURL));
                JSONArray results = response.getJSONArray("items");

                for (int i = 0; i < results.length(); i++) {
                    JSONObject entry = results.getJSONObject(i);
                    links.add(entry.getString("link"));
                }
            } catch (Exception e) {
                System.err.println("The response from the server for results " + (searchCount * RESULTS_PER_SEARCH + 1) +
                        "-" + ((searchCount + 1) * RESULTS_PER_SEARCH) + " is not valid JSON (the search performed is" +
                        "likely invalid)");
                return links; // Quit this search
            }
        }
        try {
            getCrawler().crawl(links, query);
        } catch (Exception e) {
            throw new RuntimeException("The crawler failed to crawl the " + getEngineName() + " \"" +
                    query.getQueryString() + "\" result set");
        }
        return links;
    }

    protected String getQueryString(Query query) {
        return baseURL + SearchEngine.createQueryString(processQuery(query));
    }

    protected String getResultRange(int searchCount, int maxResults) {
        TreeMap<String, String> extraParams = new TreeMap<String, String>();
        int resultsAlreadyRetrieved = searchCount * RESULTS_PER_SEARCH;
        int numOfResults = RESULTS_PER_SEARCH;
        if (maxResults - resultsAlreadyRetrieved < RESULTS_PER_SEARCH) {
            numOfResults = maxResults - resultsAlreadyRetrieved;
        }
        extraParams.put("num", numOfResults + "");
        extraParams.put("start", (resultsAlreadyRetrieved + 1) + "");
        return SearchEngine.createQueryString(extraParams);
    }

    protected TreeMap<String, String> processQuery(Query q) {
        TreeMap<String, String> queryParams = new TreeMap<String, String>();
        String required = Utils.concatenate(q.getRequiredTerms(), "+");
        String excluded = Utils.concatenate(q.getExcludedTerms(), "+");
        String optional = Utils.concatenate(q.getOptionalTerms(), "+");
        if (required.length() > 0) {
            queryParams.put("exactTerms", required);
        }
        if (excluded.length() > 0) {
            queryParams.put("excludeTerms", excluded);
        }
        queryParams.put("q", optional);
        return queryParams;
    }

    @Override
    public String getEngineName() {
        return "Google Search Engine";
    }
}
