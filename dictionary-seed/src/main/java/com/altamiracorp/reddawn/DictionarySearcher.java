package com.altamiracorp.reddawn;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
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

	public String search(String type) throws IOException, JSONException {
		StringBuilder output = new StringBuilder();
		int totalResultCount = 0;
		int resultOffset = 0;

		do {
			System.out.print("Querying... ");
			String response = httpRequest(getUrl(type, resultOffset));
			System.out.println("DONE");

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

			resultOffset += 50000;

		} while(totalResultCount - resultOffset == 0);

		System.out.println("Found " + totalResultCount + " matches for " + type);
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

	private String getUrl(String type, int offset) throws UnsupportedEncodingException {
		String query = "PREFIX dbo: <http://dbpedia.org/ontology/>\n" +
				"SELECT ?name WHERE{?place a dbo:" + type + ";foaf:name ?name.}\n" +
				"LIMIT 50000\nOFFSET " + offset;

		return baseURL + URLEncoder.encode(query, "UTF-8");

//		return baseURL + "PREFIX+dbo%3A+%3Chttp%3A%2F%2Fdbpedia.org%2Fontology%2F%3E%0D%0A%0D%0ASELECT+DISTINCT+%3Fname+" +
//				"WHERE+%7B%0D%0A+++++%3Fperson+a+dbo%3A" + type + "+%3B+foaf%3Aname+%3Fname+.%0D%0A%7D%0D%0A";
	}
}
