package com.altamiracorp.reddawn;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

/**
 * Created with IntelliJ IDEA.
 * User: jprincip
 * Date: 6/7/13
 * Time: 10:11 AM
 * To change this template use File | Settings | File Templates.
 */
public class GoogleNewsSearchEngine implements SearchEngine {

    private String baseURL;

    private ArrayList<Query> queryQueue;
    private ArrayList<Integer> maxResultQueue;

    private Crawler crawler;

    public GoogleNewsSearchEngine(Crawler c) {
        crawler = c;
        baseURL = "http://news.google.com/news?output=rss";
        queryQueue = new ArrayList<Query>();
        maxResultQueue = new ArrayList<Integer>();

    }

    @Override
    public boolean addQueryToQueue(Query q, int maxResults) {
        if(!queryQueue.add(q)) return false;
        if(maxResults < 0 || !maxResultQueue.add(maxResults)) {
            queryQueue.remove(queryQueue.size()-1);
            return false;
        }
        return true;
    }

    @Override
    public ArrayList<ArrayList<String>> runQueue() {
        ArrayList<ArrayList<String>> results = new ArrayList<ArrayList<String>>();

        for(int i = 0; i < queryQueue.size(); i++) {
            results.add(search(queryQueue.get(i), maxResultQueue.get(i)));
        }

        return results;
    }

    @Override
    public ArrayList<String> runQuery(Query q, int maxResults) {
        return search(q, maxResults);
    }

    private ArrayList<String> search(Query q, int maxResults) {
        String queryString = EngineFunctions.createQueryString(processQuery(q));

        // Result Links to return
        ArrayList<String> links = new ArrayList<String>();

        // Adds the result range to the query
        TreeMap<String, String> extraParams = new TreeMap<String, String>();
        extraParams.put("num", maxResults + "");

        // Creates query URL
        URL fullURL = null;
        try {
            fullURL = new URL(baseURL + queryString + EngineFunctions.createQueryString(extraParams));
        } catch (MalformedURLException e) {
            System.err.println("Malformed search URL");
            return null;
        }

        SAXReader saxReader = new SAXReader();
        Document xml;
        try {
           xml = saxReader.read(fullURL);
        } catch (DocumentException e) {
            System.err.println("The specified URL (" + fullURL + ") does not produce a valid document");
            return null;
        }

        List items = xml.getRootElement().element("channel").elements("item");
        for(int i = 0; i < items.size(); i++) {

            Element link = ((Element) items.get(i)).element("link");

            URL googleURL;

            try{
                googleURL = new URL(link.getStringValue());
            } catch(MalformedURLException e) {
                System.err.println("Google News provided a malformed URL. Skipping...");
                break;
            }

            for(String param : googleURL.getQuery().split("&")) {
                String[] kvPair = param.split("=");
                if(kvPair[0].equals("url")) links.add(kvPair[1]);
            }
        }

        try {
            crawler.processSearchResults(links, q);
        } catch (Exception e) {
            System.err.println("The crawler failed to crawl the result set");
            e.printStackTrace();
        }

        return links;
    }

    private TreeMap<String, String> processQuery(Query q) {
        TreeMap<String, String> queryParams = new TreeMap<String, String>();
        queryParams.put("q", EngineFunctions.concatenate(q.getOptionalTerms(), "+"));

        return queryParams;
    }

}
