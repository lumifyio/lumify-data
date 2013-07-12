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
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import javax.imageio.ImageIO;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.metadata.IIOMetadata;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class HttpRetriever implements Runnable {
    private static final Logger LOGGER = LoggerFactory.getLogger(HttpRetriever.class);

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
        Pattern jpegFileTypePattern = Pattern.compile("(.+)\\.jpg");
        Matcher jpegFileTypeMatcher = jpegFileTypePattern.matcher(url);
        if (jpegFileTypeMatcher.matches()) {
            writeImageToFile();
            return;
        } else {
            writeDocumentToFile();
            return;
        }
    }

    private void writeImageToFile() {
        BufferedImage image = getImage();
        if (image != null) {
            try {
                IIOMetadata meta = createMetadata(image);

                ImageIO.write(image, "jpg", new File(directoryPath + getFileName(new StringBuilder(url))));
            } catch (IOException e) {
                LOGGER.error("Unable to write image to file: " + url);
            }
        }
    }

    private IIOMetadata createMetadata(BufferedImage image) {
        ImageWriter writer = ImageIO.getImageWritersByFormatName("jpg").next();
        ImageWriteParam writeParam = writer.getDefaultWriteParam();
        ImageTypeSpecifier typeSpecifier = ImageTypeSpecifier.createFromBufferedImageType(image.TYPE_INT_RGB);
        IIOMetadata metadata = writer.getDefaultImageMetadata(typeSpecifier, writeParam);

          return null;
    }

    void displayMetadata(Node root) {
        displayMetadata(root, 0);
    }

    private BufferedImage getImage() {
        try {
            return ImageIO.read(new URL(url));
        } catch (Exception e) {
            LOGGER.error("Could not retrieve image from: " + url);
            return null;
        }
    }


    void displayMetadata(Node node, int level) {
        // print open tag of element
        indent(level);
        System.out.print("<" + node.getNodeName());
        NamedNodeMap map = node.getAttributes();
        if (map != null) {

            // print attribute values
            int length = map.getLength();
            for (int i = 0; i < length; i++) {
                Node attr = map.item(i);
                System.out.print(" " + attr.getNodeName() +
                        "=\"" + attr.getNodeValue() + "\"");
            }
        }

        Node child = node.getFirstChild();
        if (child == null) {
            // no children, so close element and return
            System.out.println("/>");
            return;
        }

        // children, so close current tag
        System.out.println(">");
        while (child != null) {
            // print children recursively
            displayMetadata(child, level + 1);
            child = child.getNextSibling();
        }

        // print close tag of element
        indent(level);
        System.out.println("</" + node.getNodeName() + ">");
    }


    void indent(int level) {
        for (int i = 0; i < level; i++)
            System.out.print("    ");
    }

    private void writeDocumentToFile() {
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

    private String getFileName(StringBuilder sb) {
        MessageDigest messageDigest = null;
        try {
            messageDigest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            LOGGER.error("Unable to find SHA-256 algorithm to generate file name.");
            e.printStackTrace();
        }
        byte[] bytesOfMessage = new byte[0];
        try {
            bytesOfMessage = sb.toString().getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            LOGGER.error("Unable to get UTF-8 content as bytes to generate file name.");
            e.printStackTrace();
        }
        byte[] hash = messageDigest.digest(bytesOfMessage);
        return Hex.encodeHexString(hash);
    }
}
