package com.altamiracorp.reddawn.crawler;

import java.io.File;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

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
        stringBuilder.append("<meta property=\"atc:result-url\" content=\"" + url + "\">\n");
        stringBuilder.append("<meta property=\"atc:retrieval-timestamp\" content=\"" + getCurrentTimestamp() + "\">\n");
        stringBuilder.append("<meta property=\"atc:query-info\" content=\"" + query.getQueryString() + "\">\n");
        return stringBuilder.toString();
    }

    protected HttpRetrievalManager createManager() {
        return new HttpRetrievalManager();
    }

    public long getCurrentTimestamp() {
        long unixTime = System.currentTimeMillis() / 1000L;
        return unixTime;
    }

}
