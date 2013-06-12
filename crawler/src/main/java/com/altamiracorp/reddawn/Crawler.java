package com.altamiracorp.reddawn;

import java.io.*;
import java.util.ArrayList;
import java.util.concurrent.ArrayBlockingQueue;
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
		HttpRetrievalManager manager = new HttpRetrievalManager(10, 100, 5, TimeUnit.MINUTES, new LinkedBlockingQueue<Runnable>());

		for (String url : links) {
			String header = getHeader(url,query);
			manager.addJob(url, header, directoryPath);
		}
		manager.shutdown();
		manager.awaitTermination(5, TimeUnit.MINUTES);

		System.out.println("\033[34mSearch completed.\033[0m");
	}

	private String getHeader(String url, Query query) {
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append("contentSource: " + url + "\n");
		stringBuilder.append("timeOfRetrieval: " + EngineFunctions.getCurrentTimestamp() + "\n");
		stringBuilder.append("queryInfo: " + query.getQueryInfo() + "\n");
		return stringBuilder.toString();
	}






}
