package com.altamiracorp.reddawn.crawler;

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import sun.misc.IOUtils;

import javax.imageio.*;
import javax.imageio.metadata.IIOInvalidTreeException;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataNode;
import javax.imageio.stream.ImageOutputStream;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URL;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class PhotoHttpRetriever implements Runnable {
    private static final Logger LOGGER = LoggerFactory.getLogger(PhotoHttpRetriever.class);

    private HttpClient httpClient;
    private String header;
    private String directoryPath;
 //   private String url;
  //  private HttpGet httpGet;
    private Map.Entry<String, TreeMap<String, String>> urlsAndInfo;
    private TreeMap<String, String> metaDataTags;
    private final String JPEG_NATIVE_FORMAT = "javax_imageio_jpeg_image_1.0";

    public PhotoHttpRetriever(HttpClient httpClient, String header, String directoryPath, Map.Entry<String,
            TreeMap<String, String>> urlAndMetaInfo) {
        this.httpClient = httpClient;
        this.header = header;
        this.directoryPath = directoryPath;
//        for (String url_ : urlAndMetaInfo.keySet()) {
//            this.url = url_;
//            this.metaDataTags = urlAndMetaInfo.get(url_);
//        }
       // httpGet = new HttpGet(url);
        this.urlsAndInfo = urlAndMetaInfo;
    }

    @Override
    public void run() {
        Pattern jpegFileTypePattern = Pattern.compile("(.+)\\.jpg");
        Matcher jpegFileTypeMatcher = jpegFileTypePattern.matcher(urlsAndInfo.getKey());
        if (jpegFileTypeMatcher.matches()) {
            writeImageToFile(urlsAndInfo.getKey());
            return;
        } else {
            return;
        }


    }

    private void writeImageToFile(String imageUrl) {
        BufferedImage image = getImage(imageUrl);
        if (image != null) {
            try {

                ImageWriter writer = ImageIO.getImageWritersByFormatName("jpg").next();
                ImageWriteParam writeParam = writer.getDefaultWriteParam();


                IIOMetadata meta = createMetadata(image);

                System.out.println("Back");
                IIOImage newImage = new IIOImage(image, null, meta);

                File outputFile = new File(directoryPath + Utils.getFileName(new StringBuilder(imageUrl)));
                System.out.println("About to set output");

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ImageOutputStream stream = ImageIO.createImageOutputStream(baos);
                writer.setOutput(stream);

                System.out.println("About to write");
                writer.write(meta, newImage, writeParam);

                FileOutputStream outputToFile = new FileOutputStream(new File(directoryPath +
                        Utils.getFileName(new StringBuilder(imageUrl))));
//                IOUtils.write(baos.toByteArray(), outputToFile);

                BufferedOutputStream bos = new BufferedOutputStream(outputToFile);
                bos.write(baos.toByteArray());

                System.out.println("Wrote " + imageUrl);
//                ImageIO.write(image, "jpg", new File(directoryPath + Utils.getFileName(new StringBuilder(imageUrl))));
            } catch (IOException e) {
                LOGGER.error("Unable to write image to file: " + imageUrl);
                e.printStackTrace();
            }
        }
    }

    private BufferedImage getImage(String imageUrl) {
        try {
            return ImageIO.read(new URL(imageUrl));
        } catch (Exception e) {
            LOGGER.error("Could not retrieve image from: " + imageUrl);
            return null;
        }
    }

    private IIOMetadata createMetadata(BufferedImage image) {
        System.out.println("inside create meta data");
        ImageWriter writer = ImageIO.getImageWritersByFormatName("jpg").next();
        ImageWriteParam writeParam = writer.getDefaultWriteParam();
        ImageTypeSpecifier typeSpecifier = ImageTypeSpecifier.createFromBufferedImageType(image.TYPE_INT_RGB);
        IIOMetadata metadata = writer.getDefaultImageMetadata(typeSpecifier, writeParam);
        metadata.reset();
        IIOMetadataNode root = new IIOMetadataNode(JPEG_NATIVE_FORMAT);
//        IIOMetadataNode root = (IIOMetadataNode) metadata.getAsTree(JPEG_NATIVE_FORMAT); // does not work because it doesn't have jpegvariety and markerSequence nodes
        IIOMetadataNode jv = new IIOMetadataNode("JPEGvariety");
        root.appendChild(jv);
////        for(String key : metaDataTags.keySet()) {
////            String value = metaDataTags.get(key);
////            String childNodeName = "imgChildNode";
////            IIOMetadataNode childNode = new IIOMetadataNode(JPEG_NATIVE_FORMAT);
////            childNode.setAttribute(key, value);
////            System.out.println(key + ":" + value);
////            root.appendChild(childNode);
//////            metadata.mergeTree();
////        }
        IIOMetadataNode childNode = new IIOMetadataNode("atcAwesome");
//
        childNode.setAttribute("keyword", "sampleData");
        jv.appendChild(childNode);

        try {
            metadata.mergeTree(JPEG_NATIVE_FORMAT, root);
        } catch (IIOInvalidTreeException e) {
            LOGGER.error("Unable to merge metadata into image.");
            e.printStackTrace();
        }
//        for(String name : metadata.getMetadataFormatNames()) {
//            System.out.println(name);
//        }
//        return null;
             System.out.println("Leaving create meta data");
        return metadata;
    }

    //delete all this before committing
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

}
