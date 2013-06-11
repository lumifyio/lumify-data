package com.altamiracorp.reddawn;

import org.apache.http.*;
import org.apache.http.Header;
import org.apache.http.client.HttpClient;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;

import org.apache.commons.httpclient.*;
import org.apache.commons.httpclient.methods.*;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

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
		int success = 0, error = 0;

        for (String link : links ) {
			if(processURL(link, query)) success++;
            else error++;
		}

        System.out.println("\033[34mSearch completed: " + success + " URL(s) successfully crawled, " + error + " URL(s) unsuccessfully crawled\033[0m");
	}

	/**
	 * Follows ONE URL and writes contents to file in directoryPath.
	 * @param link the URL to process
     * @return Whether or not the URL was successfully processed
	 */
    private boolean processURL(String link, Query query) throws Exception {
		StringBuilder stringBuilder = new StringBuilder();
		Timestamp currentTimestamp = getCurrentTimestamp();
		String queryInfo = query.getQueryInfo();
		String httpHeader = "";
		String fileName = "";
		BufferedWriter fwriter = null;

		stringBuilder.append("contentSource: " + link + "\n");
		stringBuilder.append("timeOfRetrieval: " + currentTimestamp + "\n");
		stringBuilder.append("queryInfo: " + queryInfo + "\n");

		HttpClient httpclient = new DefaultHttpClient();
		HttpGet httpget = new HttpGet(link);
		HttpResponse httpresponse = httpclient.execute(httpget);

		String status = httpresponse.getStatusLine().toString();
		String[] statusInfo = status.split(" ");
		int statusNumber = Integer.parseInt(statusInfo[1]);
		if (statusNumber >= 400 && statusNumber < 500)
		{
			System.err.println("\033[31m[Error] Page not found: " + link + "\033[0m");
			return false;
		}

		for (Header s : httpresponse.getAllHeaders())
		{
			stringBuilder.append(s + "\n");
		}

		HttpEntity entity = httpresponse.getEntity();
		InputStream instream = null;
		if (entity != null)
		{
			try {
				instream = entity.getContent();
				int line = 0;
				while ((line = instream.read()) != -1)
				{
					stringBuilder.append((char)line);
				}
			}
			finally
			{
				instream.close();
			}
		}
		fileName = getFileName(stringBuilder);
		File file = new File(directoryPath + fileName);
		fwriter = new BufferedWriter(new FileWriter(file));
		fwriter.append(stringBuilder);
    	fwriter.flush();
		fwriter.close();

        System.out.println("Processed: " + link);
        return true;
	}

	/**
	 * Returns the SHA-256 hash of the content.
	 * @param sb the content of the page as a string builder
	 * @return the hash as a String
	 */
	private String getFileName(StringBuilder sb) throws NoSuchAlgorithmException, UnsupportedEncodingException
	{
		MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
		byte[] bytesOfMessage = sb.toString().getBytes("UTF-8");
		byte[] hash = messageDigest.digest(bytesOfMessage);
		return bytesToHex(hash);
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

    public static String bytesToHex(byte[] bytes) {
        final char[] hexArray = {'0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F'};
        char[] hexChars = new char[bytes.length * 2];
        int v;
        for ( int j = 0; j < bytes.length; j++ ) {
            v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }
}
