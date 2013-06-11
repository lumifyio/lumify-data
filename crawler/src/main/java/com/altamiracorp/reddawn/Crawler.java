package com.altamiracorp.reddawn;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.PoolingClientConnectionManager;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;

import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class Crawler {

	private String directoryPath;

	public Crawler(String directoryPath) {
		this.directoryPath = directoryPath;
		File file = new File(directoryPath);
		if (!file.isDirectory()) {
			throw new RuntimeException("Invalid directory provided: " + directoryPath);
		}
	}

	public Crawler() {
		directoryPath = ".";
	}

	public void crawl(ArrayList<String> links, Query query) throws Exception {
		SchemeRegistry schemeRegistry = new SchemeRegistry();
		schemeRegistry.register(new Scheme("http", 80, PlainSocketFactory.getSocketFactory()));
		schemeRegistry.register(new Scheme("https", 443, SSLSocketFactory.getSocketFactory()));
		ClientConnectionManager connectionManager = new PoolingClientConnectionManager(schemeRegistry);
		HttpClient httpClient = new DefaultHttpClient(connectionManager);

		GetThread[] threads = createHttpConnectionThreads(links, httpClient, query);
		runThreads(threads);

		httpClient.getConnectionManager().shutdown();
		System.out.println("\033[34mSearch completed.\033[0m");
	}

	// Talked to Ryan about making this protected so we can mock it out
	private GetThread[] createHttpConnectionThreads(ArrayList<String> links, HttpClient httpClient, Query query) {
		String[] urls = new String[links.size()];
		for (int i = 0; i < links.size(); i++) {
			urls[i] = links.get(i);
		}
		GetThread[] threads = new GetThread[urls.length];
		for (int i = 0; i < threads.length; i++) {
			HttpGet httpGet = new HttpGet(urls[i]);
			threads[i] = new GetThread(httpClient, httpGet, query, directoryPath);
		}
		return threads;
	}

	private void runThreads(GetThread[] threads) throws InterruptedException {
		for (int j = 0; j < threads.length; j++) {
			threads[j].start();
		}
		for (int j = 0; j < threads.length; j++) {
			threads[j].join();
		}
	}

	static class GetThread extends Thread {
		private final HttpClient httpClient;
		private final HttpContext context;
		private final HttpGet httpget;
		private Query query;
		private String directoryPath;

		public GetThread(HttpClient httpClient, HttpGet httpget, Query query, String directoryPath) {
			this.httpClient = httpClient;
			this.context = new BasicHttpContext();
			this.httpget = httpget;
			this.query = query;
			this.directoryPath = directoryPath;
		}

		@Override
		public void run() {
			StringBuilder stringBuilder = getContent(this.context, this.httpget);
			if (writeToFile(stringBuilder)) {
				System.out.println("Processed: " + this.httpget.getURI());
			} else {
				System.err.println("\033[31m[Error] Problem writing file to: " + directoryPath + "\033[0m");
			}
		}

		private Timestamp getCurrentTimestamp() {
			Calendar calendar = Calendar.getInstance();
			Date now = calendar.getTime();
			return new Timestamp(now.getTime());
		}

		private String getFileName(StringBuilder sb) throws NoSuchAlgorithmException, UnsupportedEncodingException {
			MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
			byte[] bytesOfMessage = sb.toString().getBytes("UTF-8");
			byte[] hash = messageDigest.digest(bytesOfMessage);
			return bytesToHex(hash);
		}

		public static String bytesToHex(byte[] bytes) {
			final char[] hexArray = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};
			char[] hexChars = new char[bytes.length * 2];
			int v;
			for (int j = 0; j < bytes.length; j++) {
				v = bytes[j] & 0xFF;
				hexChars[j * 2] = hexArray[v >>> 4];
				hexChars[j * 2 + 1] = hexArray[v & 0x0F];
			}
			return new String(hexChars);
		}

		private boolean isSuccessfulConnection(HttpResponse response) {
			String status = response.getStatusLine().toString();
			String[] statusInfo = status.split(" ");
			int statusNumber = Integer.parseInt(statusInfo[1]);
			if (statusNumber >= 400 && statusNumber < 500) {
				System.err.println("\033[31m[Error] Page not found: " + this.httpget.getURI() + "\033[0m");
				return false;
			}
			return true;
		}

		private StringBuilder getContent(HttpContext httpContext, HttpGet httpGet) {
			StringBuilder stringBuilder = new StringBuilder();
			stringBuilder.append("contentSource: " + this.httpget.getURI() + "\n");
			stringBuilder.append("timeOfRetrieval: " + getCurrentTimestamp() + "\n");
			stringBuilder.append("queryInfo: " + query.getQueryInfo() + "\n");
			try {
				HttpResponse response = this.httpClient.execute(this.httpget, this.context);
				if (!isSuccessfulConnection(response)) {
					return stringBuilder;
				}
				for (Header s : response.getAllHeaders()) {
					stringBuilder.append(s + "\n");
				}
				HttpEntity entity = response.getEntity();
				InputStream instream = null;
				if (entity != null) {
					instream = entity.getContent();
					int line = 0;
					while ((line = instream.read()) != -1) {
						stringBuilder.append((char) line);
					}
				}
				EntityUtils.consume(entity);
				instream.close();

			} catch (IOException ex) {
				this.httpget.abort();
				System.err.println("\033[31m[Error] Problem with Http Request on URL: " + this.httpget.getURI() + "\033[0m");
			}
			return stringBuilder;
		}

		private boolean writeToFile(StringBuilder stringBuilder) {
			String fileName = "";
			BufferedWriter fwriter = null;
			try {
				fileName = getFileName(stringBuilder);
				File file = new File(directoryPath + fileName);
				fwriter = new BufferedWriter(new FileWriter(file));
				fwriter.append(stringBuilder);
				fwriter.flush();
				fwriter.close();
			} catch (Exception e) {
				return false;
			}
			return true;
		}

	}
}
