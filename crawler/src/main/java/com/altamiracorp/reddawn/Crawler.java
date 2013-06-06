package com.altamiracorp.reddawn;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: swoloszy
 * Date: 6/6/13
 * Time: 9:21 AM
 * To change this template use File | Settings | File Templates.
 */
public class Crawler {

    private ArrayList<String> links;

    /**
     * Constructor takes in an ArrayList of urls.
     * @param links_ the links to follow and crawl
     */
    public Crawler(ArrayList<String> links_)
    {
        links = links_;
    }

    /**
     *
     * @return
     */
    public boolean crawl(String link) {
		try
		{
			int line = 0;
			StringBuilder stringBuilder = new StringBuilder();
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
			System.out.println("Problem with URL.");
			return false;
		}
		catch (java.io.IOException e)
		{
			  System.out.println("Problem with connection.");
			return false;
		}
		return true;
    }

}
