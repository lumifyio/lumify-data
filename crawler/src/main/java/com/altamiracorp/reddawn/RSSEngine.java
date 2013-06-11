package com.altamiracorp.reddawn;

import javax.swing.text.Document;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: swoloszy
 * Date: 6/10/13
 * Time: 12:25 PM
 * To change this template use File | Settings | File Templates.
 */
public class RSSEngine extends SearchEngine {
	private String url;

	public RSSEngine(Crawler c) {
		super(c);
		url = "";
	}

	@Override
	protected ArrayList<String> search(Query q, int maxResults)
	{
		url = q.getRss();
		ArrayList<String> links = null;
		if (url.equals(""))
		{
		   System.err.println("No RSS URL specified");
		}
		else
		{
			URL theUrl;
			try
			{
				theUrl = new URL(url);
			}
			catch(MalformedURLException e)
			{
				System.err.println("Malformed search URL");
				return null;
			}
			links = EngineFunctions.parseRSS(theUrl, maxResults);
			// Runs the results into the crawler, which processes them and writes them to the file system
			try {
				getCrawler().processSearchResults(links, q);
			} catch (Exception e) {
				System.err.println("The crawler failed to crawl the result set");
				e.printStackTrace();
			}
		}
		return links;
	}

	/**
	 * Returns the Engine name as a string.
	 * @return engine name
	 */
	@Override
	public String getEngineName()
	{
		return "RSS Engine";
	}

}
