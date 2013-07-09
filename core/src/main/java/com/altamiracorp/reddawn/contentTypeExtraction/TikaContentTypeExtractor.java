package com.altamiracorp.reddawn.contentTypeExtraction;

import org.apache.hadoop.mapreduce.Mapper;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.sax.BodyContentHandler;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class TikaContentTypeExtractor implements ContentTypeExtractor {
    private static final String MIME_TYPE_KEY = "Content-Type";
    private static final String PROPS_FILE = "tika-extractor.properties";

    @Override
    public void setup(Mapper.Context context) {
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

    @Override
    public String extract(InputStream in, String fileExt) throws Exception {
        Parser parser = new AutoDetectParser();
        BodyContentHandler handler = new BodyContentHandler(10000000);
        Metadata metadata = new Metadata();
        ParseContext ctx = new ParseContext();
        parser.parse(in, handler, metadata, ctx);

        String contentType = metadata.get(MIME_TYPE_KEY);
        if (contentType == null || contentType.equals("application/octet-stream")) {
            contentType = setContentTypeUsingFileExt (fileExt.toLowerCase());
        }
        return contentType;
    }

    private String setContentTypeUsingFileExt (String fileExt) {
        if (fileExt.equals("jpeg") || fileExt.equals("tiff") || fileExt.equals("raw") || fileExt.equals("gif") ||
                fileExt.equals("bmp") || fileExt.equals("png")){
            return "image";
        }
        if (fileExt.equals("flv") || fileExt.equals("avi") || fileExt.equals("m2v") || fileExt.equals("mov") ||
                fileExt.equals("mpg") || fileExt.equals("wmv")){
            return "video";
        }
        return "";
    }
}
