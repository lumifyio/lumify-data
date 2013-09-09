package com.altamiracorp.lumify.crawler;

import org.apache.http.client.HttpClient;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.PoolingClientConnectionManager;

import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class HttpRetrievalManager {
    HttpClient httpClient;

    ExecutorService executor;

    public HttpRetrievalManager() {
        executor = Executors.newFixedThreadPool(10);
        SchemeRegistry schemeRegistry = new SchemeRegistry();
        schemeRegistry.register(
                new Scheme("http", 80, PlainSocketFactory.getSocketFactory()));
        schemeRegistry.register(new Scheme("https", 443, org.apache.http.conn.ssl.SSLSocketFactory.getSocketFactory()));
        ClientConnectionManager cm = new PoolingClientConnectionManager(schemeRegistry);
        httpClient = new DefaultHttpClient(cm);
    }

    public void addJob(String url, String queryInfo, String directoryPath) {
        HttpRetriever httpRetriever = new HttpRetriever(httpClient, queryInfo, directoryPath, url);
        executor.submit(httpRetriever);
    }

    public void addPhotoJob(Map.Entry<String, TreeMap<String, String>> urlAndInfo, String queryInfo, String directoryPath) {
        PhotoHttpRetriever photoHttpRetriever =  new PhotoHttpRetriever(httpClient, queryInfo, directoryPath, urlAndInfo);
        executor.submit(photoHttpRetriever);
    }

    public void shutDownWhenFinished() throws InterruptedException {
        executor.shutdown();
        executor.awaitTermination(5, TimeUnit.MINUTES);
    }
}
