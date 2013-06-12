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
		executor = Executors.newFixedThreadPool(10);
		SchemeRegistry schemeRegistry = new SchemeRegistry();
		schemeRegistry.register(
				new Scheme("http", 80, PlainSocketFactory.getSocketFactory()));

		ClientConnectionManager cm = new PoolingClientConnectionManager(schemeRegistry);
		httpClient = new DefaultHttpClient(cm);
	}

	public void addJob(String url, String header, String directoryPath) {
		HttpRetriever httpRetriever = createHttpRetriever(httpClient, header, directoryPath, url);
		executor.submit(httpRetriever);
	}

	public void shutDownWhenFinished() throws InterruptedException{
		executor.shutdown();
		executor.awaitTermination(5, TimeUnit.MINUTES);
	}

	protected HttpRetriever createHttpRetriever(HttpClient httpClient, String header, String directoryPath, String url) {
		return new HttpRetriever(httpClient, header, directoryPath,url);
	}
}
