package io.lumify.dictionary;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;

public class DictionarySearcher {
    private static final Logger LOGGER = LoggerFactory.getLogger(DictionarySearcher.class.getName());

    public static final String RESOURCE = "Resource";
    public static final String PLACE = "Place";
    public static final String PERSON = "Person";
    public static final String WORK = "Work";
    public static final String SPECIES = "Species";
    public static final String ORGANIZATION = "Organisation";

    private final int MAX_RESULTS_PER_SEARCH = 50000;

    private String baseURL = "http://dbpedia.org/sparql/?format=json&query=";
    private ArrayList<DictionaryEncoder> encoders = new ArrayList<DictionaryEncoder>();

    public void search(String type) {
        int totalResultCount = 0;
        int resultOffset = 0;

        do {
            LOGGER.info("Fetching results " + (resultOffset + 1) + "-" +
                    (resultOffset + MAX_RESULTS_PER_SEARCH) + "... ");
            String response = httpRequest(getUrl(type, resultOffset));

            try {
                totalResultCount += processJson(response);
            } catch (JSONException e) {
                LOGGER.error("Could not parse the result set for the search of " + type +
                        ", offset: " + resultOffset);
                throw new RuntimeException(e);
            }

            resultOffset += MAX_RESULTS_PER_SEARCH;

        } while (totalResultCount == resultOffset);

        LOGGER.info("Found " + totalResultCount + " matches for \"" + type + "\"");
    }

    protected String httpRequest(String url) {
        StringBuilder response = new StringBuilder();

        try {
            URL query = new URL(url);
            URLConnection connection = query.openConnection();
            BufferedReader inputStream = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String line;
            while ((line = inputStream.readLine()) != null) {
                response.append(line);
            }
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return response.toString();
    }

    protected int processJson(String response) throws JSONException {
        JSONObject json = new JSONObject(response);
        JSONArray results = json.getJSONObject("results").getJSONArray("bindings");
        String[] terms = new String[results.length()];

        for (int i = 0; i < results.length(); i++) {
            terms[i] = results.getJSONObject(i).getJSONObject("name").getString("value");
        }

        notifyEncoders(terms);
        return results.length();
    }

    public void addEncoder(DictionaryEncoder encoder) {
        encoders.add(encoder);
    }

    private void notifyEncoders(String[] terms) {
        for (DictionaryEncoder encoder : encoders) {
            encoder.addEntries(terms);
        }
    }

    protected String getUrl(String type, int offset) {
        String query = "PREFIX dbo: <http://dbpedia.org/ontology/>\n" +
                "SELECT ?name WHERE{?place a dbo:" + type + ";rdfs:label ?name.}\n" +
                "LIMIT 50000\nOFFSET " + offset;

        String url;
        try {
            url = baseURL + URLEncoder.encode(query, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            LOGGER.error("The search URL " + query + " could not be encoded properly");
            throw new RuntimeException(e);
        }
        return url;
    }
}
