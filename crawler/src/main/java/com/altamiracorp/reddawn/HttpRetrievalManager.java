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
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class HttpRetrievalManager extends ThreadPoolExecutor {
	HttpClient httpClient;


	public HttpRetrievalManager(int corePoolSize, int maxPoolSize, long keepAliveTime,
								TimeUnit timeUnit, BlockingQueue<Runnable> runnables) {
		super(corePoolSize, maxPoolSize, keepAliveTime, timeUnit, runnables);
		SchemeRegistry schemeRegistry = new SchemeRegistry();
		schemeRegistry.register(
				new Scheme("http", 80, PlainSocketFactory.getSocketFactory()));

		ClientConnectionManager cm = new PoolingClientConnectionManager(schemeRegistry);
		httpClient = new DefaultHttpClient(cm);
	}

	public void addJob(String url, String header, String directoryPath) {
		HttpRetriever httpRetriever = new HttpRetriever(httpClient, header, directoryPath,url);
		execute(httpRetriever);
	}

}
