package com.altamiracorp.lumify.contentTypeExtraction;

import com.altamiracorp.lumify.core.contentTypeExtraction.ContentTypeExtractor;
import com.altamiracorp.lumify.core.model.ontology.DisplayType;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.tika.detect.DefaultDetector;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.mime.MediaType;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;

public class TikaContentTypeExtractor implements ContentTypeExtractor {
    private static final String PROPS_FILE = "tika-extractor.properties";

    @Override
    public String extract(InputStream in, String fileExt) throws Exception {
        DefaultDetector detector = new DefaultDetector();
        Metadata metadata = new Metadata();
        MediaType mediaType = detector.detect(new BufferedInputStream(in), metadata);

        String contentType = mediaType.toString();
        if (contentType == null || contentType.equals("application/octet-stream")) {
            contentType = setContentTypeUsingFileExt(fileExt.toLowerCase());
        }
        return contentType;
    }

    @Override
    public void init(Map map) {
        Properties tikaProperties = new Properties();
        try {
            InputStream propsIn = Thread.currentThread().getContextClassLoader().getResourceAsStream(PROPS_FILE);
            if (propsIn != null) {
                tikaProperties.load(propsIn);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String setContentTypeUsingFileExt(String fileExt) {
        if (fileExt.equals("jpeg") || fileExt.equals("tiff") || fileExt.equals("raw") || fileExt.equals("gif") ||
                fileExt.equals("bmp") || fileExt.equals("png")) {
            return DisplayType.IMAGE.toString();
        }
        if (fileExt.equals("flv") || fileExt.equals("avi") || fileExt.equals("m2v") || fileExt.equals("mov") ||
                fileExt.equals("mpg") || fileExt.equals("wmv")) {
            return DisplayType.VIDEO.toString();
        }
        if (fileExt.equals("wav") || fileExt.equals("mp3")) {
            return DisplayType.AUDIO.toString();
        }
        return "";
    }
}
