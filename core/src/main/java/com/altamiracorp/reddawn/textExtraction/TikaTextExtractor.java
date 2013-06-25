package com.altamiracorp.reddawn.textExtraction;

import com.altamiracorp.reddawn.textExtraction.util.GenericDateExtractor;
import com.altamiracorp.reddawn.textExtraction.util.TikaMetadataUtils;
import de.l3s.boilerpipe.BoilerpipeProcessingException;
import de.l3s.boilerpipe.extractors.ArticleExtractor;
import de.l3s.boilerpipe.extractors.NumWordsRulesExtractor;
import org.apache.commons.io.IOUtils;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.sax.BodyContentHandler;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Properties;

public class TikaTextExtractor implements TextExtractor {

    private static final String MIME_TYPE_KEY = "Content-Type";

    private static final String PROPS_FILE = "tika-extractor.properties";
    private static final String DATE_KEYS_PROPERTY = "tika.extraction.datekeys";
    private static final String SUBJECT_KEYS_PROPERTY = "tika.extraction.titlekeys";
    private static final String URL_KEYS_PROPERTY = "tike.extraction.urlkeys";
    private static final String TYPE_KEYS_PROPERTY = "tike.extraction.typekeys";
    private static final String EXT_URL_KEYS_PROPERTY = "tike.extraction.exturlkeys";
    private static final String SRC_TYPE_KEYS_PROPERTY = "tike.extraction.srctypekeys";
    private static final String RETRIEVAL_TIMESTAMP_KEYS_PROPERTY = "tike.extraction.retrievaltimestampkeys";

    private List<String> dateKeys;
    private List<String> subjectKeys;
    private List<String> urlKeys;
    private List<String> typeKeys;
    private List<String> extUrlKeys;
    private List<String> srcTypeKeys;
    private List<String> retrievalTimestampKeys;

    @Override
    public void setup(Mapper.Context context) {
    }

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

    }

    @Override
    public ExtractedInfo extract(InputStream in) throws Exception {
        ExtractedInfo result = new ExtractedInfo();

        String text = IOUtils.toString(in);
        Parser parser = new AutoDetectParser();
        BodyContentHandler handler = new BodyContentHandler(10000000);
        Metadata metadata = new Metadata();
        ParseContext ctx = new ParseContext();
        parser.parse(new ByteArrayInputStream(text.getBytes()), handler, metadata, ctx);

        // since we are using the AutoDetectParser, it is safe to assume that
        // the Content-Type metadata key will always return a value
        String mimeType = metadata.get(MIME_TYPE_KEY);
        if (mimeType == null) {
            mimeType = "";
        }

        if (mimeType.toLowerCase().contains("text/html")) {
            text = extractTextFromHtml(text);
            if (text == null || text.length() == 0) {
                text = handler.toString();
            }
        } else {
            text = handler.toString();
        }

        result.setText(text);
        result.setMediaType(mimeType);

        result.setDate(extractDate(metadata));
        result.setSubject(extractTextField(metadata, subjectKeys));
        result.setUrl(extractUrl(metadata));
        result.setType(extractTextField(metadata, typeKeys));
        result.setExtUrl(extractTextField(metadata, extUrlKeys));
        result.setSrcType(extractTextField(metadata, srcTypeKeys));
        result.setRetrievalTime(extractRetrievalTime(metadata));

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

    // TODO: add domain extraction magic here
    private String extractUrl(Metadata metadata) {
        // find the title metadata property, if there is one
        String url = "";
        String urlKey = TikaMetadataUtils.findKey(urlKeys, metadata);

        if (urlKey != null) {
            url = metadata.get(urlKey);
        }

        return url;
    }

    private Long extractRetrievalTime(Metadata metadata) {
        Long retrievalTime = 0l;
        String retrievalTimeKey = TikaMetadataUtils.findKey(retrievalTimestampKeys, metadata);

        if(retrievalTimeKey != null) {
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
}
