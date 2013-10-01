package com.altamiracorp.lumify.textExtraction;

import com.altamiracorp.lumify.textExtraction.util.GenericDateExtractor;
import com.altamiracorp.lumify.textExtraction.util.TikaMetadataUtils;
import com.google.inject.Inject;
import de.l3s.boilerpipe.BoilerpipeProcessingException;
import de.l3s.boilerpipe.extractors.ArticleExtractor;
import de.l3s.boilerpipe.extractors.NumWordsRulesExtractor;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.sax.BodyContentHandler;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Properties;

public class TikaTextExtractor {
    public static final int WRITE_LIMIT = 1000000;

    private static final Logger LOGGER = LoggerFactory.getLogger(TikaTextExtractor.class);
    private static final String NAME = "tikaExtractor";

    private static final String PROPS_FILE = "tika-extractor.properties";
    private static final String DATE_KEYS_PROPERTY = "tika.extraction.datekeys";
    private static final String SUBJECT_KEYS_PROPERTY = "tika.extraction.titlekeys";
    private static final String URL_KEYS_PROPERTY = "tika.extraction.urlkeys";
    private static final String TYPE_KEYS_PROPERTY = "tika.extraction.typekeys";
    private static final String EXT_URL_KEYS_PROPERTY = "tika.extraction.exturlkeys";
    private static final String SRC_TYPE_KEYS_PROPERTY = "tika.extraction.srctypekeys";
    private static final String RETRIEVAL_TIMESTAMP_KEYS_PROPERTY = "tika.extraction.retrievaltimestampkeys";
    private static final String CUSTOM_FLICKR_METADATA_KEYS_PROPERTY = "tika.extraction.customflickrmetadatakeys";

    private List<String> dateKeys;
    private List<String> subjectKeys;
    private List<String> urlKeys;
    private List<String> typeKeys;
    private List<String> extUrlKeys;
    private List<String> srcTypeKeys;
    private List<String> retrievalTimestampKeys;
    private List<String> customFlickrMetadataKeys;

    @Inject
    public TikaTextExtractor() {
        // TODO: Create an actual properties class?
        Properties tikaProperties = new Properties();
        try {
            // don't require the properties file
            InputStream propsIn = Thread.currentThread()
                    .getContextClassLoader().getResourceAsStream(PROPS_FILE);
            if (propsIn != null) {
                tikaProperties.load(propsIn);
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        dateKeys = Arrays.asList(tikaProperties.getProperty(DATE_KEYS_PROPERTY, "date,published,pubdate,publish_date,last-modified, atc:last-modified").split(","));
        subjectKeys = Arrays.asList(tikaProperties.getProperty(SUBJECT_KEYS_PROPERTY, "title,subject").split(","));
        urlKeys = Arrays.asList(tikaProperties.getProperty(URL_KEYS_PROPERTY, "url,og:url").split(","));
        typeKeys = Arrays.asList(tikaProperties.getProperty(TYPE_KEYS_PROPERTY, "Content-Type").split(","));
        extUrlKeys = Arrays.asList(tikaProperties.getProperty(EXT_URL_KEYS_PROPERTY, "atc:result-url").split(","));
        srcTypeKeys = Arrays.asList(tikaProperties.getProperty(SRC_TYPE_KEYS_PROPERTY, "og:type").split(","));
        retrievalTimestampKeys = Arrays.asList(tikaProperties.getProperty(RETRIEVAL_TIMESTAMP_KEYS_PROPERTY, "atc:retrieval-timestamp").split(","));
        customFlickrMetadataKeys = Arrays.asList(tikaProperties.getProperty(CUSTOM_FLICKR_METADATA_KEYS_PROPERTY, "Unknown tag (0x9286)").split(","));

    }

    public ArtifactExtractedInfo extract(String sourceName, InputStream in, String mimeType) throws Exception {
        ArtifactExtractedInfo result = new ArtifactExtractedInfo();
        Parser parser = new AutoDetectParser(); // TODO: the content type should already be detected. To speed this up we should be able to grab the parser from content type.
        String text = "";
        ContentHandler handler = new BodyContentHandler(WRITE_LIMIT);
        Metadata metadata = new Metadata();
        ParseContext ctx = new ParseContext();

        try {
            parser.parse(in, handler, metadata, ctx);
        } catch (SAXException ex) {
            // TODO: this exception occures after we reached the write limit specified above
            LOGGER.warn("Failed to process all the data for: " + sourceName, ex);
        }

        // since we are using the AutoDetectParser, it is safe to assume that
        //the Content-Type metadata key will always return a value
        if (isHtml(mimeType)) {
            text = extractTextFromHtml(text);
            if (text == null || text.length() == 0) {
                text = handler.toString();
            }
        } else {
            text = handler.toString();
        }

        result.setText(text);

        result.setDate(extractDate(metadata));
        result.setSubject(extractTextField(metadata, subjectKeys));
        result.setUrl(extractUrl(metadata));
        result.setType(extractTextField(metadata, typeKeys));
        result.setExtUrl(extractTextField(metadata, extUrlKeys));
        result.setSrcType(extractTextField(metadata, srcTypeKeys));
        result.setRetrievalTime(extractRetrievalTime(metadata));

        String customImageMetadata = extractTextField(metadata, customFlickrMetadataKeys);
        if (customImageMetadata != null && !customImageMetadata.equals("")) {
            try {
                JSONObject customImageMetadataJson = new JSONObject(customImageMetadata);
                result.setText(new JSONObject(customImageMetadataJson.get("description").toString()).get("_content") +
                        "\n" + customImageMetadataJson.get("tags").toString());
                result.setDate(GenericDateExtractor
                        .extractSingleDate(customImageMetadataJson.get("lastupdate").toString()));
                result.setRetrievalTime(Long.parseLong(customImageMetadataJson.get("atc:retrieval-timestamp").toString()));
                result.setSubject(customImageMetadataJson.get("title").toString());
            } catch (JSONException e) {
                LOGGER.warn("Image returned invalid custom metadata");
            }
        }

        return result;
    }

    private String extractTextFromHtml(String text) throws BoilerpipeProcessingException {
        String extractedText;

        extractedText = NumWordsRulesExtractor.INSTANCE.getText(text);
        if (extractedText != null && extractedText.length() > 0) {
            return extractedText;
        }

        extractedText = ArticleExtractor.INSTANCE.getText(text);
        if (extractedText != null && extractedText.length() > 0) {
            return extractedText;
        }

        return null;
    }

    private Date extractDate(Metadata metadata) {
        // find the date metadata property, if there is one
        String dateKey = TikaMetadataUtils.findKey(dateKeys, metadata);
        Date date = null;
        if (dateKey != null) {
            date = GenericDateExtractor
                    .extractSingleDate(metadata.get(dateKey));
        }

        if (date == null) {
            date = new Date();
        }

        return date;
    }

    private Long extractRetrievalTime(Metadata metadata) {
        Long retrievalTime = 0l;
        String retrievalTimeKey = TikaMetadataUtils.findKey(retrievalTimestampKeys, metadata);

        if (retrievalTimeKey != null) {
            retrievalTime = Long.parseLong(metadata.get(retrievalTimeKey));
        }

        return retrievalTime;
    }

    private String extractTextField(Metadata metadata, List<String> keys) {
        // find the title metadata property, if there is one
        String field = "";
        String fieldKey = TikaMetadataUtils.findKey(keys, metadata);

        if (fieldKey != null) {
            field = metadata.get(fieldKey);
        }

        return field;
    }

    private String extractUrl(Metadata metadata) {
        // find the url metadata property, if there is one; strip down to domain name
        String urlKey = TikaMetadataUtils.findKey(urlKeys, metadata);
        String host = "";
        if (urlKey != null) {
            String url = metadata.get(urlKey);
            try {
                URL netUrl = new URL(url);
                host = netUrl.getHost();
                if (host.startsWith("www")) {
                    host = host.substring("www".length() + 1);
                }
            } catch (MalformedURLException e) {
                throw new RuntimeException("Bad url: " + url);
            }
        }
        return host;
    }

    private boolean isHtml(String mimeType) {
        return mimeType.contains("html");
    }

}
