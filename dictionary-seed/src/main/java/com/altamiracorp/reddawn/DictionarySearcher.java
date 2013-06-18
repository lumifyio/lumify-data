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

public class DictionarySearcher {
	public static final String ALL = "Resource",
			PLACE = "Place",
			PERSON = "Person",
			WORK = "Work",
			SPECIES = "Species",
			ORGANIZATION = "Organisation";

	public String baseURL;

	public DictionarySearcher() {
		baseURL = "http://dbpedia.org/sparql/?format=json&query=";
	}

	public String search(String type) {
		StringBuilder output = new StringBuilder();
		int totalResultCount = 0;
		int resultOffset = 0;
        final int MAX_RESULTS_PER_SEARCH = 50000;

        System.out.println("Querying... ");

        do {
            System.out.print("Fetching results " + (resultOffset + 1) + "-" +
                    (resultOffset + MAX_RESULTS_PER_SEARCH) + "... ");
			String response = httpRequest(getUrl(type, resultOffset));
            System.out.println("DONE");
            try {
                JSONObject json = new JSONObject(response.toString());
                JSONArray results = json.getJSONObject("results").getJSONArray("bindings");

                totalResultCount += results.length();

                for (int i = 0; i < results.length(); i++) {
                    String name = results.getJSONObject(i).getJSONObject("name").getString("value");
                    output.append(name);
                    if (i != results.length() - 1) {
                        output.append("\n");
                    }
                }
            } catch (JSONException e) {
                throw new RuntimeException("Could not parse the result set for the search of " + type +
                        ", offset: " + resultOffset);
            }

			resultOffset += MAX_RESULTS_PER_SEARCH;

		} while(totalResultCount - resultOffset == 0);

        System.out.println("Found " + totalResultCount + " matches for \"" + type + "\"");
		return output.toString();
	}

	private String httpRequest(String url) {
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

	private String getUrl(String type, int offset) {
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
