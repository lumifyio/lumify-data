package com.altamiracorp.reddawn;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

/**
 * Created with IntelliJ IDEA.
 * User: swoloszy
 * Date: 6/6/13
 * Time: 9:21 AM
 * To change this template use File | Settings | File Templates.
 */
public class Crawler {

    private ArrayList<String> links;
	private Timestamp timestamp;

    /**
     * Constructor takes in an ArrayList of urls.
     * @param links_ the links to follow and crawl
     */
    public Crawler(ArrayList<String> links_)
    {
        links = links_;

    }

    /**
     * Returns the content of ONE url as a String Builder
     * @param link the link to follow
	 * @return the page's content
     */
    public StringBuilder crawl(String link) throws Exception {
		int line = 0;
		StringBuilder stringBuilder = new StringBuilder();
		String urlName = EngineFunctions.toSlug(link);

		Calendar calendar = Calendar.getInstance();
		Date now = calendar.getTime();
		timestamp = new Timestamp(now.getTime());

		stringBuilder.append("URL: " + link + "\n");
		stringBuilder.append("Cleaned URL: " + urlName + "\n");
		stringBuilder.append("Timestamp: " + timestamp + "\n");

		try
		{
			URL url = new URL(link);
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			BufferedReader in
                = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			BufferedReader reader = new BufferedReader(in);
			if (reader != null)
			{
				while ((line = reader.read()) != -1)
				{
				   	stringBuilder.append((char) line);
				}
				reader.close();
			}
			in.close();
			System.out.println(stringBuilder.toString());
		}
		catch (MalformedURLException e)
		{
			throw new MalformedURLException("Problem with URL.");
		}
		catch (java.io.IOException e)
		{
			  throw new IOException("Problem with connection.");
		}

		return stringBuilder;
    }

	public boolean createFile(StringBuilder sb)
	{

		return false;
	}

}
