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

/**
 * Created with IntelliJ IDEA.
 * User: jprincip
 * Date: 6/6/13
 * Time: 1:11 PM
 * To change this template use File | Settings | File Templates.
 */
public class EngineFunctions {
	private static final Pattern NONLATIN = Pattern.compile("[^\\w-]");
	private static final Pattern WHITESPACE = Pattern.compile("[\\s]");

	/**
     * Concatenates the strings in the list, separated by the connector specified
     *
     * @param list Terms to concatenate
     * @param connector String to insert in between each value
     * @return Concatenated string of terms
     */
    public static String concatenate(ArrayList<String> list, String connector) {
        String ret = "";
        for(String entry : list) {
            if(ret.length() > 0) ret += connector;
            ret += entry.replace(" ", connector);
        }
        return ret;
    }

    /**
     * Generates the query string parameters to be tacked on to the request URL
     *
     * @param params Map of Google Custom Search API keys to their values
     * @return Query string to append to URL
     */
    public static String createQueryString(Map<String, String> params) {
        String ret = "";

        // Adds GET variable to the query for each parameter in the map
        for(Map.Entry<String, String> entry : params.entrySet()) {
            ret += "&" + entry.getKey() + "=" + entry.getValue();
        }

        return ret;
    }

	/**
	 * Creates a clean version of a url to be used as a directory name.
	 * @param input the url to be cleaned
	 * @return the cleaned string
	 */
	public static String toSlug(String input) {
		String nowhitespace = WHITESPACE.matcher(input).replaceAll("-");
		String normalized = Normalizer.normalize(nowhitespace, Normalizer.Form.NFD);
		String slug = NONLATIN.matcher(normalized).replaceAll("");
		return slug.toLowerCase(Locale.ENGLISH);
	}

	public static String searchWithGetRequest(String queryURL)
	{
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
			connection.addRequestProperty("Referer", "altamiracorp.com");
			reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			while((line = reader.readLine()) != null) {
				builder.append(line);
			}
		} catch(IOException e) {
			System.err.println("The http connection failed");
			return null;
		}
		return builder.toString();
	}

	/**
	 * Reads the RSS Document into an XML file to parse (output should be RSS XML)
	 * Parses the XML to find links, returns a list of links
	 * @param url
	 * @return a list of links
	 */

	public static ArrayList<String> parseRSS(URL url, int maxResults)
	{
		// Result Links to return
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
		if (maxResults > items.size())
		{
			loopLimit = items.size();
		}
		for(int i = 0; i < loopLimit; i++) {
			// Gets Feed link with redirect to the link we want
			Element link = ((Element) items.get(i)).element("link");
			//RSS in general
			links.add(link.getStringValue());
		}
		return links;
	}

}
