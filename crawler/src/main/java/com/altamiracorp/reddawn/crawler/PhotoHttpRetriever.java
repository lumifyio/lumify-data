package com.altamiracorp.reddawn.crawler;

import org.apache.http.client.HttpClient;
import org.apache.sanselan.ImageReadException;
import org.apache.sanselan.ImageWriteException;
import org.apache.sanselan.Sanselan;
import org.apache.sanselan.common.IImageMetadata;
import org.apache.sanselan.formats.jpeg.JpegImageMetadata;
import org.apache.sanselan.formats.jpeg.exifRewrite.ExifRewriter;
import org.apache.sanselan.formats.tiff.TiffImageMetadata;
import org.apache.sanselan.formats.tiff.constants.ExifTagConstants;
import org.apache.sanselan.formats.tiff.write.TiffOutputDirectory;
import org.apache.sanselan.formats.tiff.write.TiffOutputField;
import org.apache.sanselan.formats.tiff.write.TiffOutputSet;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
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
    private String queryInfo;
    private String directoryPath;
    private String url;
    private TreeMap<String, String> metaDataTags;

    public PhotoHttpRetriever(HttpClient httpClient, String queryInfo, String directoryPath, Map.Entry<String,
            TreeMap<String, String>> urlAndMetaInfo) {
        this.httpClient = httpClient;
        this.queryInfo = queryInfo;
        this.directoryPath = directoryPath;
        this.url = urlAndMetaInfo.getKey();
        this.metaDataTags = urlAndMetaInfo.getValue();
    }

    @Override
    public void run() {
        Pattern jpegFileTypePattern = Pattern.compile("(.+)\\.jpg");
        Matcher jpegFileTypeMatcher = jpegFileTypePattern.matcher(url);
        if (jpegFileTypeMatcher.matches()) {
            writeImageToFile(url);
            return;
        } else {
            return;
        }
    }

    private void writeImageToFile(String imageUrl) {
        BufferedImage image = getImage(imageUrl);
        if (image != null) {
            try {
                String filename = Utils.getFileName(new StringBuilder(imageUrl));
                File inputFile = new File(directoryPath + "/o" + filename);
                ImageIO.write(image, "jpg", inputFile);
                File outputFile = new File(directoryPath + "/" + filename);
                changeExifMetadata(inputFile, outputFile);
                inputFile.delete();
                System.out.println("Processed: " + url);
            } catch (Exception e) {
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

    public void changeExifMetadata(final File jpegImageFileInput, final File outputFile)
            throws IOException, ImageReadException, ImageWriteException {
        OutputStream os = null;
        try {
            TiffOutputSet outputSet = getTiffOutputSet(jpegImageFileInput);
            TiffOutputDirectory exifDirectory = outputSet.getOrCreateRootDirectory();

            exifDirectory.removeField(ExifTagConstants.EXIF_TAG_USER_COMMENT);
            TiffOutputField field = exifDirectory.findField(ExifTagConstants.EXIF_TAG_USER_COMMENT);
            final String commentString = getMetadataAsJson();

            TiffOutputField tiffOutputField = new TiffOutputField(
                    ExifTagConstants.EXIF_TAG_USER_COMMENT,
                    ExifTagConstants.FIELD_TYPE_ASCII,
                    commentString.length(),
                    commentString.getBytes());

            exifDirectory.add(tiffOutputField);

            os = new FileOutputStream(outputFile);
            os = new BufferedOutputStream(os);

            new ExifRewriter().updateExifMetadataLossless(jpegImageFileInput, os,
                    outputSet);

            os.close();
            os = null;
        } finally {
            if (os != null) {
                try {
                    os.close();
                } catch (final IOException e) {

                }
            }
        }
    }

    private String getMetadataAsJson() {
        JSONObject json = new JSONObject();
        for (String key : metaDataTags.keySet()) {
            try {
                json.put(key, metaDataTags.get(key));
            } catch (JSONException e) {
                LOGGER.error("Could not write " + key + " metadata to url " + url);
            }
        }
        return json.toString();
    }

    private TiffOutputSet getTiffOutputSet(File jpegImageFile) throws ImageReadException, IOException, ImageWriteException {
        TiffOutputSet outputSet = null;
        // note that metadata might be null if no metadata is found.
        final IImageMetadata metadata = Sanselan.getMetadata(jpegImageFile);
        final JpegImageMetadata jpegMetadata = (JpegImageMetadata) metadata;
        if (null != jpegMetadata) {
            // note that exif might be null if no Exif metadata is found.
            final TiffImageMetadata exif = jpegMetadata.getExif();
            if (null != exif) {
                outputSet = exif.getOutputSet();
            }
        }
        if (null == outputSet) {   // if no exif metadata, create empty set. else keep existing.
            outputSet = new TiffOutputSet();
        }
        return outputSet;
    }

}
