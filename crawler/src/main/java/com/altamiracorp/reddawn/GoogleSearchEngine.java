package com.altamiracorp.reddawn;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

/**
 * Created with IntelliJ IDEA.
 * User: jprincip
 * Date: 6/5/13
 * Time: 3:07 PM
 * To change this template use File | Settings | File Templates.
 */
public class GoogleSearchEngine implements SearchEngine {

    public static final int GOOGLE_NEWS = 1,
                            GOOGLE_WEB = 2,
                            GOOGLE_VIDEO = 3,
                            GOOGLE_IMAGE = 4,
                            GOOGLE_BLOG = 5;

    private String baseURL;

    public GoogleSearchEngine() {
        baseURL = "https://www.googleapis.com/customsearch/v1?key=AIzaSyB4H5oZoRFCVsNoYUNI6nCNAMAusD1GpDY";
    }

    @Override
    public boolean addQueryToQueue(Query q, int maxResults) {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public ArrayList<ArrayList<String>> runQueue() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    /**
     * Runs the passed query on the search engine, returning the results
     *
     * @param q The query to execute
     * @param maxResults The maximum number of results that should be returned
     * @return ArrayList of URLs found in the search
     */
    @Override
    public ArrayList<String> runQuery(Query q, int maxResults) throws JSONException {
        // Holds key-value pairs for query
        TreeMap<String, String> queryParams = new TreeMap<String, String>();

        // Adds key-value pairs to the query variable map
        queryParams.put("alt", "json");
        queryParams.put("q", "boston+bombing");
        queryParams.put("cx", "012249192867828703671:vknw0znfgfa");

        // Result Links to return
        ArrayList<String> links = new ArrayList<String>();

        for(int searchCount = 0; searchCount*10 < maxResults; searchCount++) {
            // Adds the result ranges to the query
            queryParams.put("num", (maxResults - searchCount * 10 < 10 ? maxResults - searchCount * 10 : 10) + "");
            queryParams.put("start", searchCount * 10 + 1 + "");

            // Creates query URL
            URL fullURL = null;
            try {
                fullURL = new URL(baseURL + createQueryString(queryParams));
            } catch (MalformedURLException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }

            // Connects to the internet at the queryURL
            URLConnection connection;
            String line;
            StringBuilder builder = new StringBuilder();
            BufferedReader reader;
            try {
                connection = fullURL.openConnection();
                connection.addRequestProperty("Referer", "altamiracorp.com");
                reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                while((line = reader.readLine()) != null) {
                    builder.append(line);
                }
            } catch(IOException e) {
                System.err.println("The connection failed");
                e.printStackTrace();
            }

            // Get response from page and put it in a JSON object (return type should be JSON)
            JSONObject response = new JSONObject(builder.toString());
            JSONArray results = response.getJSONArray("items");

            for(int i = 0; i < results.length(); i++) {
                JSONObject entry = results.getJSONObject(i);
                links.add(entry.getString("link"));
            }
        }

        System.out.println(links);

        return links;
    }

    /**
     * Generates the query string parameters to be tacked on to the request URL
     *
     * @param params Map of Google Custom Search API keys to their values
     * @return Query string to append to URL
     */
    private String createQueryString(Map<String, String> params) {
        String ret = "";

        // Adds GET variable to the query for each parameter in the map
        for(Map.Entry<String, String> entry : params.entrySet()) {
            ret += "&" + entry.getKey() + "=" + entry.getValue();
        }

        return ret;
    }
}
