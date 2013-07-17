package com.altamiracorp.reddawn.crawler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

public class RedditSearchEngine extends SearchEngine {
    private static final Logger LOGGER = LoggerFactory.getLogger(RedditSearchEngine.class);

    public RedditSearchEngine(Crawler crawler) {
        super(crawler);
    }

    @Override
    protected List<String> search(Query q, int maxResults) {
        ArrayList<String> searchResults = new ArrayList<String>();
        String queryUrl = createQueryString(q, maxResults);

        try {
            JSONObject resultsJSON = new JSONObject(SearchEngine.getWebpage(queryUrl));
            JSONArray childrenEntries = resultsJSON.getJSONObject("data").getJSONArray("children");
            for (int i = 0; i < maxResults; i++) {
                searchResults.add(childrenEntries.getJSONObject(i).getJSONObject("data").getString("url"));
            }
        } catch (JSONException e) {
            LOGGER.error("Results not successfully processed as JSON");
            return searchResults;
        }
        try {
            getCrawler().crawl(searchResults, q);
        } catch (Exception e) {
            LOGGER.error("The crawler failed to crawl the " + getEngineName() + " on query \"" +
                    q.getQueryString() + "\" result set");
        }

        return searchResults;
    }

    protected String createQueryString(Query q, int maxResults) {
        String url = "http://www.reddit.com/";
        ArrayList<String> terms = new ArrayList<String>();

        for (String s : q.getOptionalTerms()) {
            terms.add(s);
        }
        for (String s : q.getRequiredTerms()) {
            terms.add(s);
        }

        if (terms.size() == 0) {
            if (!q.getSubreddit().equals("")) {
                url += "r/" + q.getSubreddit() + "/";
            }
            url += ".json?limit=" + maxResults;
        } else {
            if (!q.getSubreddit().equals("")) {
                url += "r/" + q.getSubreddit() + "/";
            }
            url += "search.json?limit=" + maxResults + "&q=" + Utils.concatenate(terms, "+");
            if (!q.getSubreddit().equals("")) {
                url += "&restrict_sr=true";
            }
        }

        return url;
    }

    @Override
    public String getEngineName() {
        return "Reddit Search Engine";
    }
}