package com.altamiracorp.reddawn.crawler;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;

import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class HttpRetriever implements Runnable {
    private HttpClient httpClient;
    private String header;
    private String directoryPath;
    private String url;
    private HttpGet httpGet;

    public HttpRetriever(HttpClient httpClient, String header, String directoryPath, String url) {
        this.httpClient = httpClient;
        this.header = header;
        this.directoryPath = directoryPath;
        this.url = url;
        httpGet = new HttpGet(url);
    }

    @Override
    public void run() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(header);
        StringBuilder content = getContent();
        if (content.toString().equals("")) {
            return;
        }
        stringBuilder.append(content);
        if (writeToFile(stringBuilder)) {
            System.out.println("Processed: " + url);
        } else {
            System.err.println("\033[31m[Error] Problem writing file to: " + directoryPath + "\033[0m");
        }
    }

    private boolean isSuccessfulConnection(HttpResponse response) {
        String status = response.getStatusLine().toString();
        String[] statusInfo = status.split(" ");
        int statusNumber = Integer.parseInt(statusInfo[1]);
        if (statusNumber >= 400 && statusNumber < 500) {
            System.err.println("\033[31m[Error] Page not found: " + httpGet.getURI() + "\033[0m");
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
            System.err.println("\033[31m[Error] Problem with Http Request on URL: " + httpGet.getURI() + "\033[0m");
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
            String fileName = getFileName(stringBuilder);
            File file = new File(directoryPath + fileName);
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

    private String getFileName(StringBuilder sb) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
        byte[] bytesOfMessage = sb.toString().getBytes("UTF-8");
        byte[] hash = messageDigest.digest(bytesOfMessage);
        return Hex.encodeHexString(hash);
    }
}
