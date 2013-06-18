package com.altamiracorp.reddawn;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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
	public static final String ALL = "Resource",
                                PLACE = "Place",
                                PERSON = "Person",
                                WORK = "Work",
                                SPECIES = "Species",
                                ORGANIZATION = "Organisation";

    private final int MAX_RESULTS_PER_SEARCH = 50000,
                        BUFFER_SIZE = 1000;

	private String baseURL = "http://dbpedia.org/sparql/?format=json&query=";
    private ArrayList<DictionaryEncoder> encoders = new ArrayList<DictionaryEncoder>();

	public void search(String type) {
		int totalResultCount = 0;
		int resultOffset = 0;

        System.out.println("Searching for dbpedia class: " + type);

        do {
            System.out.print("Fetching results " + (resultOffset + 1) + "-" +
                    (resultOffset + MAX_RESULTS_PER_SEARCH) + "... ");
			String response = httpRequest(getUrl(type, resultOffset));
            System.out.println("DONE");

            try {
                totalResultCount += processJson(response);
            } catch (JSONException e) {
                throw new RuntimeException("Could not parse the result set for the search of " + type +
                        ", offset: " + resultOffset);
            }

			resultOffset += MAX_RESULTS_PER_SEARCH;

		} while(totalResultCount == resultOffset);

        System.out.println("Found " + totalResultCount + " matches for \"" + type + "\"");
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
            throw new RuntimeException("The search URL was malformed");
        } catch (IOException e) {
            throw new RuntimeException("Could not fetch search results from server");
        }

        return response.toString();
	}

    protected int processJson(String response) throws JSONException {
        StringBuilder buffer = new StringBuilder();

        JSONObject json = new JSONObject(response);
        JSONArray results = json.getJSONObject("results").getJSONArray("bindings");

        int bufferLength = 0;
        for (int i = 0; i < results.length(); i++) {
            String name = results.getJSONObject(i).getJSONObject("name").getString("value");
            buffer.append(name);
            buffer.append("\n");

            if(++bufferLength == BUFFER_SIZE || i == (results.length() - 1)) {
                notifyEncoders(buffer.toString());
                buffer = new StringBuilder();
                bufferLength = 0;
            }
        }

        return results.length();
    }

    public void addEncoder(DictionaryEncoder encoder) {
        encoders.add(encoder);
    }

    private void notifyEncoders(String terms) {
        for(DictionaryEncoder encoder : encoders) {
            encoder.addEntries(terms);
        }
    }

	protected String getUrl(String type, int offset) {
		String query = "PREFIX dbo: <http://dbpedia.org/ontology/>\n" +
				"SELECT ?name WHERE{?place a dbo:" + type + ";foaf:name ?name.}\n" +
				"LIMIT 50000\nOFFSET " + offset;

		String url;
        try {
            url = baseURL + URLEncoder.encode(query, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("The search URL " + query + " could not be encoded properly");
        }
        return url;
	}
}
