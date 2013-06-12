package com.altamiracorp.reddawn;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

public class EngineFunctions {
	private static final Pattern NONLATIN = Pattern.compile("[^\\w-]");
	private static final Pattern WHITESPACE = Pattern.compile("[\\s]");

	/**
	 * Concatenates the strings in the list, separated by the connector specified
	 *
	 * @param list      Terms to concatenate
	 * @param connector String to insert in between each value
	 * @return Concatenated string of terms
	 */
	public static String concatenate(ArrayList<String> list, String connector) {
		String ret = "";
		for (String entry : list) {
			if (ret.length() > 0) ret += connector;
			ret += entry.replace(" ", connector);
		}
		return ret;
	}

	public static String createQueryString(Map<String, String> params) {
		String ret = "";

		// Adds GET variable to the query for each parameter in the map
		for (Map.Entry<String, String> entry : params.entrySet()) {
			ret += "&" + entry.getKey() + "=" + entry.getValue();
		}

		return ret;
	}

	/**
	 * This method is intended for use by the Search Engines
	 * in order to get the results of a search without following the links returned.
	 * It uses URLConnection and does not follow redirects.
	 * @param queryURL
	 * @return
	 */
	public static String getWebpage(String queryURL) {
		// Creates query URL
		URL fullURL = null;
		try {
			fullURL = new URL(queryURL);
		} catch (MalformedURLException e) {
			System.err.println("Malformed search URL");
			return null;
		}

		// Connects to the internet at the queryURL
		URLConnection connection;
		String line;
		StringBuilder builder = new StringBuilder();
		BufferedReader reader;
		try {
			connection = fullURL.openConnection();
			reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			while ((line = reader.readLine()) != null) {
				builder.append(line);
			}
		} catch (IOException e) {
			System.err.println("The http connection failed");
			return null;
		}
		return builder.toString();
	}

	public static ArrayList<String> parseRSS(URL url, int maxResults) {
		ArrayList<String> links = new ArrayList<String>();
		SAXReader saxReader = new SAXReader();
		Document xml;
		try {
			xml = saxReader.read(url);
		} catch (DocumentException e) {
			System.err.println("The specified URL (" + url + ") does not produce a valid document");
			return null;
		}
		List items = xml.getRootElement().element("channel").elements("item");
		int loopLimit = maxResults;
		if (maxResults > items.size()) {
			loopLimit = items.size();
		}
		for (int i = 0; i < loopLimit; i++) {
			Element link = ((Element) items.get(i)).element("link");
			links.add(link.getStringValue());
		}
		return links;
	}

}
