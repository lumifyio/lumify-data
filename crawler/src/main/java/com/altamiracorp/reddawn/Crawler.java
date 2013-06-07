package com.altamiracorp.reddawn;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
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

	private String directoryPath;

	/**
     *  Constructor for Crawler.
	 *  Allows directoryPath to write files to to be specified.
	 *  @param directory_ the directoryPath to write files to
     */
    public Crawler(String directory_)
    {
	   	directoryPath = directory_;
    }

	/**
	 * Default constructor for Crawler.
	 * Sets the directoryPath to write files to to the current directoryPath where the program is being run.
	 */
	public Crawler()
	{
		URL location = Crawler.class.getProtectionDomain().getCodeSource().getLocation();
		directoryPath = location.getFile();
	}

	/**
	 * Follows links of search results and writes content to files
	 * in the specified or current (default) directoryPath under a file name created by hashing (MD5) the contents.
	 * Iterates through all results.
	 * Content format is:
	 * contentSource: {[URL]}, timeOfRetrieval: {[Timestamp]}, queryInfo: {[query meta data]},
	 * httpHeader: {[http header]}, content: {[html content]}
	 * @param links An ArrayList of Strings of URLs of search results
	 * @param query the query that produced the results
	 */
	public void processSearchResults(ArrayList<String> links, Query query) throws Exception {
		for (String link : links )
		{
			processURL(link);
		}
	}

	/**
	 * Follows ONE URL and writes contents to file in directoryPath.
	 * @param link the URL to process
	 */
    private void processURL(String link) throws Exception {
		int line = 0;
		StringBuilder stringBuilder = new StringBuilder();
		Timestamp currentTimestamp = getCurrentTimestamp();
		String queryInfo = "{Query.getInfo() not yet implemented.}"; //query.getInfo();
		String httpHeader = "";
		String fileName = "";
		BufferedWriter fwriter = null;

		stringBuilder.append("contentSource: {" + link + "}, ");
		stringBuilder.append("timeOfRetrieval: {" + currentTimestamp + "}, ");
		stringBuilder.append("queryInfo: " + queryInfo + ", ");
		try
		{
			URL url = new URL(link);
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			BufferedReader in
                = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			BufferedReader reader = new BufferedReader(in);

			httpHeader = connection.getHeaderFields().toString();
			stringBuilder.append("httpHeader: " + httpHeader + ", ");
			stringBuilder.append("content: {");

			if (reader != null)
			{
				while ((line = reader.read()) != -1)
				{
				   	stringBuilder.append((char) line);
				}
				reader.close();
			}
			in.close();
		}
		catch (MalformedURLException e)
		{
			throw new MalformedURLException("Problem with URL.");
		}
		catch (java.io.IOException e)
		{
			  throw new IOException("Problem with connection.");
		}

		stringBuilder.append("}");
		fileName = getFileName(stringBuilder);
		File file = new File(directoryPath + fileName);
		fwriter = new BufferedWriter(new FileWriter(file));
		fwriter.append(stringBuilder);
    	fwriter.flush();
		fwriter.close();
	}

	/**
	 * Returns the MD5 hash of the content.
	 * @param sb the content of the page as a string builder
	 * @return the hash as a String
	 */
	private String getFileName(StringBuilder sb) throws NoSuchAlgorithmException, UnsupportedEncodingException
	{
		MessageDigest messageDigest = MessageDigest.getInstance("MD5");
		byte[] bytesOfMessage = sb.toString().getBytes("UTF-8");
		byte[] hash = messageDigest.digest(bytesOfMessage);
		return hash.toString();
	}

	/**
	 * Helper method returns the current Timestamp
	 * @return the current Timestamp
	 */
	private Timestamp getCurrentTimestamp()
	{
		Calendar calendar = Calendar.getInstance();
		Date now = calendar.getTime();
		return new Timestamp(now.getTime());
	}
}
