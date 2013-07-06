package com.altamiracorp.reddawn.contentTypeExtraction;

import org.apache.hadoop.mapreduce.Mapper;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.sax.BodyContentHandler;

import java.util.Properties;

import java.io.IOException;
import java.io.InputStream;

public class TikaContentTypeExtractor implements ContentTypeExtractor{
    private static final String MIME_TYPE_KEY = "Content-Type";
    private static final String PROPS_FILE = "tika-extractor.properties";

    @Override
    public void setup (Mapper.Context context) {
        Properties tikaProperties = new Properties ();
        try {
            InputStream propsIn = Thread.currentThread().getContextClassLoader().getResourceAsStream(PROPS_FILE);
            if (propsIn != null) {
                tikaProperties.load(propsIn);
            }
        }
        catch (IOException e){
            e.printStackTrace();
        }
    }

    @Override
    public String extract (InputStream in) throws Exception {
        Parser parser = new AutoDetectParser();
        BodyContentHandler handler = new BodyContentHandler();
        Metadata metadata = new Metadata();
        ParseContext ctx = new ParseContext();
        parser.parse(in,  handler, metadata, ctx);

        String contentType = metadata.get(MIME_TYPE_KEY);
        if (contentType == null){
            contentType = "";
        }
        return contentType;
    }
}
