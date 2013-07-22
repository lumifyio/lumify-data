package com.altamiracorp.reddawn.crawler;

import org.apache.commons.codec.binary.Hex;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;


public class HttpRetriever implements Runnable {
    private static final Logger LOGGER = LoggerFactory.getLogger(HttpRetriever.class);

    private HttpClient httpClient;
    private String queryInfo;
    private String directoryPath;
    private String url;
    private HttpGet httpGet;

    public HttpRetriever(HttpClient httpClient, String queryInfo, String directoryPath, String url) {
        this.httpClient = httpClient;
        this.queryInfo = queryInfo;
        this.directoryPath = directoryPath;
        this.url = url;
        httpGet = new HttpGet(url);
    }

    @Override
    public void run() {
        writeDocumentToFile();
    }

    private void writeDocumentToFile() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(getHeader(queryInfo));
        StringBuilder content = getContent();
        if (content.toString().equals("")) {
            return;
        }
        stringBuilder.append(content);
        if (writeToFile(stringBuilder)) {
            LOGGER.info("Processed: " + url);
        } else {
            LOGGER.error("\033[31m[Error] Problem writing file to: " + directoryPath + "\033[0m");
        }
    }

    private String getHeader(String queryInfo) {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("<meta property=\"atc:result-url\" content=\"" + url + "\">\n");
            stringBuilder.append("<meta property=\"atc:retrieval-timestamp\" content=\"" + getCurrentTimestamp() + "\">\n");
            stringBuilder.append("<meta property=\"atc:query-info\" content=\"" + queryInfo + "\">\n");
            return stringBuilder.toString();
    }

    public long getCurrentTimestamp() {
        long unixTime = System.currentTimeMillis() / 1000L;
        return unixTime;
    }

    private boolean isSuccessfulConnection(HttpResponse response) {
        String status = response.getStatusLine().toString();
        String[] statusInfo = status.split(" ");
        int statusNumber = Integer.parseInt(statusInfo[1]);
        if (statusNumber >= 400 && statusNumber < 500) {
            LOGGER.error("\033[31m[Error] Page not found: " + httpGet.getURI() + "\033[0m");
            return false;
        }
        return true;
    }

    private StringBuilder getContent() {
        StringBuilder stringBuilder = new StringBuilder();
        try {
            HttpResponse response = httpClient.execute(httpGet);
            if (!isSuccessfulConnection(response)) {
                return stringBuilder;
            }
            stringBuilder.append(getLastModified(response));
            stringBuilder.append(getResponseContent(response));
        } catch (IOException ex) {
            httpGet.abort();
            LOGGER.error("\033[31m[Error] Problem with Http Request on URL: " + httpGet.getURI() + "\033[0m");
            return new StringBuilder();
        }
        return stringBuilder;
    }

    public String getLastModified(HttpResponse response) {
        String tag = "";
        for (Header s : response.getAllHeaders()) {
            if (s.getName().contains("Last-Modified")) {
                tag = "<meta property=\"atc:last-modified\" content=\"" + s.getValue() + "\">\n";
                return tag;
            }
        }
        return tag;
    }

    private String getResponseContent(HttpResponse response) throws IOException {
        StringBuilder stringBuilder = new StringBuilder();
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
        return stringBuilder.toString();
    }

    private boolean writeToFile(StringBuilder stringBuilder) {
        BufferedWriter fwriter = null;
        try {
            String fileName = Utils.getFileName(stringBuilder);
            File file = new File(directoryPath + "/" + fileName);
            fwriter = new BufferedWriter(new FileWriter(file));
            fwriter.append(stringBuilder);
        } catch (Exception e) {
            return false;
        } finally {
            try {
                fwriter.flush();
                fwriter.close();
            } catch (IOException e) {
                return false;
            }
        }
        return true;
    }


}
