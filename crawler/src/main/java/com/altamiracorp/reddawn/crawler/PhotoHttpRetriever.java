package com.altamiracorp.reddawn.crawler;

import com.google.common.io.Files;
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
                String filename = Utils.getFileName(new StringBuilder(imageUrl));
                File inputFile = new File(directoryPath + "/o" + filename);
                ImageIO.write(image, "jpg", inputFile);
                File outputFile = new File(directoryPath + "/" + filename);
                changeExifMetadata(inputFile, outputFile);
                inputFile.delete();
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
            final String commentString = "This is a comment";

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

        // if file does not contain any exif metadata, we create an empty
        // set of exif metadata. Otherwise, we keep all of the other
        // existing tags.
        if (null == outputSet) {
            outputSet = new TiffOutputSet();
        }
        return outputSet;
    }

}
