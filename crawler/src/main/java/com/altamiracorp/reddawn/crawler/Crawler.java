package com.altamiracorp.reddawn.crawler;

import java.io.File;
import java.sql.Timestamp;
import java.util.*;

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
        HttpRetrievalManager manager = new HttpRetrievalManager();
        for (String url : links) {
            String queryInfo = query.getQueryInfo();
            manager.addJob(url, queryInfo, directoryPath);
        }
        manager.shutDownWhenFinished();
        System.out.println("\033[34mSearch completed.\033[0m");
    }

    public void crawlPhotos(TreeMap<String, TreeMap<String, String>> links, Query query) throws Exception {
        HttpRetrievalManager manager = new HttpRetrievalManager();
        for (Map.Entry<String, TreeMap<String, String>> entry : links.entrySet()) {
            entry.getValue().put("atc:retrieval-timestamp", "" + getCurrentTimestamp());
            manager.addPhotoJob(entry, query.getQueryInfo(), directoryPath);
        }
        manager.shutDownWhenFinished();
        System.out.println("\033[34mSearch completed.\033[0m");
    }

    public long getCurrentTimestamp() {
        long unixTime = System.currentTimeMillis() / 1000L;
        return unixTime;
    }

}
