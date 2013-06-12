package com.altamiracorp.reddawn;

import org.apache.http.client.HttpClient;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.PoolingClientConnectionManager;

import java.util.concurrent.*;

public class HttpRetrievalManager {
	HttpClient httpClient;

	ExecutorService executor;
	public HttpRetrievalManager() {
		restartExecutor();
		SchemeRegistry schemeRegistry = new SchemeRegistry();
		schemeRegistry.register(
				new Scheme("http", 80, PlainSocketFactory.getSocketFactory()));

		ClientConnectionManager cm = new PoolingClientConnectionManager(schemeRegistry);
		httpClient = new DefaultHttpClient(cm);
	}

	public void addJob(String url, String header, String directoryPath) {
		HttpRetriever httpRetriever = new HttpRetriever(httpClient, header, directoryPath,url);
		executor.submit(httpRetriever);
	}

	public void restartExecutor() {
		executor = Executors.newFixedThreadPool(10);

	}

	public void shutDownWhenFinished() throws InterruptedException{
		executor.shutdown();
		executor.awaitTermination(5, TimeUnit.MINUTES);
	}
}
