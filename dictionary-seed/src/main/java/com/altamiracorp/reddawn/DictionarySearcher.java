package com.altamiracorp.reddawn;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

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

	public String search(String type) throws IOException, JSONException {
		String response = httpRequest(getUrl(type));

		JSONObject json = new JSONObject(response.toString());
		JSONArray results = json.getJSONObject("results").getJSONArray("bindings");

		StringBuilder output = new StringBuilder();

		for (int i = 0; i < results.length(); i++) {
			String name = results.getJSONObject(i).getJSONObject("name").getString("value");
			output.append(name);
			if (i != results.length() - 1) {
				output.append("\n");
			}
		}

		return output.toString();
	}

	private String httpRequest(String url) throws IOException {
		URL query = new URL(url);
		URLConnection connection = query.openConnection();
		BufferedReader inputStream = new BufferedReader(new InputStreamReader(connection.getInputStream()));
		StringBuilder response = new StringBuilder();
		String line;
		while ((line = inputStream.readLine()) != null) {
			response.append(line);
		}

		return response.toString();
	}

	private String getUrl(String type) {
		return baseURL + "PREFIX+dbo%3A+%3Chttp%3A%2F%2Fdbpedia.org%2Fontology%2F%3E%0D%0A%0D%0ASELECT+DISTINCT+%3Fname+" +
				"WHERE+%7B%0D%0A+++++%3Fperson+a+dbo%3A" + type + "+%3B+foaf%3Aname+%3Fname+.%0D%0A%7D%0D%0A";
	}
}
