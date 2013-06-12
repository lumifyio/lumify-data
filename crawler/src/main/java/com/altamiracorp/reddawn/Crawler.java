package com.altamiracorp.reddawn;

import java.io.File;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class Crawler {

	private String directoryPath;

	public Crawler(String directoryPath) {
		this();
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
		HttpRetrievalManager manager = createManager();
		for (String url : links) {
			String header = getHeader(url, query);
			manager.addJob(url, header, directoryPath);
		}
		manager.shutDownWhenFinished();
		System.out.println("\033[34mSearch completed.\033[0m");
	}

	protected String getHeader(String url, Query query) {
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append("contentSource: " + url + "\n");
		stringBuilder.append("timeOfRetrieval: " + getCurrentTimestamp() + "\n");
		stringBuilder.append("queryInfo: " + query.getQueryInfo() + "\n");
		return stringBuilder.toString();
	}

	protected HttpRetrievalManager createManager() {
		return new HttpRetrievalManager();
	}

	public Timestamp getCurrentTimestamp() {
		Calendar calendar = Calendar.getInstance();
		Date now = calendar.getTime();
		return new Timestamp(now.getTime());
	}

}
