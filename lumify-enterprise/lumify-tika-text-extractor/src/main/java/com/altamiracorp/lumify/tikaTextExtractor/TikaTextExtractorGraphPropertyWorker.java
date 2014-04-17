package com.altamiracorp.lumify.tikaTextExtractor;

import com.altamiracorp.lumify.core.ingest.graphProperty.GraphPropertyWorkData;
import com.altamiracorp.lumify.core.ingest.graphProperty.GraphPropertyWorker;
import com.altamiracorp.lumify.core.ingest.graphProperty.GraphPropertyWorkerPrepareData;
import com.altamiracorp.lumify.core.model.properties.LumifyProperties;
import com.altamiracorp.lumify.core.model.properties.RawLumifyProperties;
import com.altamiracorp.lumify.core.util.LumifyLogger;
import com.altamiracorp.lumify.core.util.LumifyLoggerFactory;
import com.altamiracorp.securegraph.Property;
import com.altamiracorp.securegraph.Vertex;
import com.altamiracorp.securegraph.mutation.ExistingElementMutation;
import com.altamiracorp.securegraph.property.StreamingPropertyValue;
import de.l3s.boilerpipe.BoilerpipeProcessingException;
import de.l3s.boilerpipe.extractors.ArticleExtractor;
import de.l3s.boilerpipe.extractors.NumWordsRulesExtractor;
import org.apache.commons.io.IOUtils;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.sax.BodyContentHandler;
import org.json.JSONException;
import org.json.JSONObject;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.*;
import java.util.regex.Pattern;

import static com.google.common.base.Preconditions.checkNotNull;

public class TikaTextExtractorGraphPropertyWorker extends GraphPropertyWorker {
    private static final LumifyLogger LOGGER = LumifyLoggerFactory.getLogger(TikaTextExtractorGraphPropertyWorker.class);

    private static final String MULTIVALUE_KEY = TikaTextExtractorGraphPropertyWorker.class.getName();

    private static final String PROPS_FILE = "tika-extractor.properties";
    private static final String DATE_KEYS_PROPERTY = "tika.extraction.datekeys";
    private static final String SUBJECT_KEYS_PROPERTY = "tika.extraction.titlekeys";
    private static final String AUTHOR_PROPERTY = "tika.extractions.author";
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
    private List<String> authorKeys;

    @Override
    public void prepare(GraphPropertyWorkerPrepareData workerPrepareData) throws Exception {
        super.prepare(workerPrepareData);

        // TODO: Create an actual properties class?
        Properties tikaProperties = new Properties();
        try {
            // don't require the properties file
            InputStream propsIn = Thread.currentThread().getContextClassLoader().getResourceAsStream(PROPS_FILE);
            if (propsIn != null) {
                tikaProperties.load(propsIn);
            }
        } catch (IOException e) {
            LOGGER.error("Could not load config: %s", PROPS_FILE);
        }

        dateKeys = Arrays.asList(tikaProperties.getProperty(DATE_KEYS_PROPERTY, "date,published,pubdate,publish_date,last-modified, atc:last-modified").split(","));
        subjectKeys = Arrays.asList(tikaProperties.getProperty(SUBJECT_KEYS_PROPERTY, "title,subject").split(","));
        urlKeys = Arrays.asList(tikaProperties.getProperty(URL_KEYS_PROPERTY, "url,og:url").split(","));
        typeKeys = Arrays.asList(tikaProperties.getProperty(TYPE_KEYS_PROPERTY, "Content-Type").split(","));
        extUrlKeys = Arrays.asList(tikaProperties.getProperty(EXT_URL_KEYS_PROPERTY, "atc:result-url").split(","));
        srcTypeKeys = Arrays.asList(tikaProperties.getProperty(SRC_TYPE_KEYS_PROPERTY, "og:type").split(","));
        retrievalTimestampKeys = Arrays.asList(tikaProperties.getProperty(RETRIEVAL_TIMESTAMP_KEYS_PROPERTY, "atc:retrieval-timestamp").split(","));
        customFlickrMetadataKeys = Arrays.asList(tikaProperties.getProperty(CUSTOM_FLICKR_METADATA_KEYS_PROPERTY, "Unknown tag (0x9286)").split(","));
        authorKeys = Arrays.asList(tikaProperties.getProperty(AUTHOR_PROPERTY, "author").split(","));
    }

    @Override
    public void execute(InputStream in, GraphPropertyWorkData data) throws Exception {
        String mimeType = (String) data.getProperty().getMetadata().get(RawLumifyProperties.METADATA_MIME_TYPE);
        checkNotNull(mimeType, RawLumifyProperties.METADATA_MIME_TYPE + " is a required metadata field");

        Metadata metadata = new Metadata();
        String text = extractText(in, mimeType, metadata);

        ExistingElementMutation<Vertex> m = data.getVertex().prepareMutation();

        // TODO set("url", extractUrl(metadata));
        // TODO set("type", extractTextField(metadata, typeKeys));
        // TODO set("extUrl", extractTextField(metadata, extUrlKeys));
        // TODO set("srcType", extractTextField(metadata, srcTypeKeys));

        String author = extractTextField(metadata, authorKeys);
        if (author != null && author.length() > 0) {
            RawLumifyProperties.AUTHOR.addPropertyValue(m, MULTIVALUE_KEY, author, data.getPropertyMetadata(), data.getVisibility());
        }

        String customImageMetadata = extractTextField(metadata, customFlickrMetadataKeys);
        Map<String, Object> textMetadata = data.getPropertyMetadata();
        textMetadata.put(RawLumifyProperties.METADATA_MIME_TYPE, "text/plain");

        if (customImageMetadata != null && !customImageMetadata.equals("")) {
            try {
                JSONObject customImageMetadataJson = new JSONObject(customImageMetadata);

                text = new JSONObject(customImageMetadataJson.get("description").toString()).get("_content") +
                        "\n" + customImageMetadataJson.get("tags").toString();
                StreamingPropertyValue textValue = new StreamingPropertyValue(new ByteArrayInputStream(text.getBytes()), String.class);
                RawLumifyProperties.TEXT.addPropertyValue(m, MULTIVALUE_KEY, textValue, textMetadata, data.getVisibility());

                Date lastupdate = GenericDateExtractor
                        .extractSingleDate(customImageMetadataJson.get("lastupdate").toString());
                RawLumifyProperties.CREATE_DATE.addPropertyValue(m, MULTIVALUE_KEY, lastupdate, data.getPropertyMetadata(), data.getVisibility());

                // TODO set("retrievalTime", Long.parseLong(customImageMetadataJson.get("atc:retrieval-timestamp").toString()));

                LumifyProperties.TITLE.addPropertyValue(m, MULTIVALUE_KEY, customImageMetadataJson.get("title").toString(), data.getPropertyMetadata(), data.getVisibility());
            } catch (JSONException e) {
                LOGGER.warn("Image returned invalid custom metadata");
            }
        } else {
            StreamingPropertyValue textValue = new StreamingPropertyValue(new ByteArrayInputStream(text.getBytes()), String.class);
            RawLumifyProperties.TEXT.addPropertyValue(m, MULTIVALUE_KEY, textValue, textMetadata, data.getVisibility());

            RawLumifyProperties.CREATE_DATE.addPropertyValue(m, MULTIVALUE_KEY, extractDate(metadata), data.getPropertyMetadata(), data.getVisibility());
            String title = extractTextField(metadata, subjectKeys).replaceAll(",", " ");
            if (title != null && title.length() > 0) {
                LumifyProperties.TITLE.addPropertyValue(m, MULTIVALUE_KEY, title, data.getPropertyMetadata(), data.getVisibility());
            }

            // TODO set("retrievalTime", extractRetrievalTime(metadata));
        }

        m.save();
        getGraph().flush();
        getWorkQueueRepository().pushGraphPropertyQueue(data.getVertex().getId(), MULTIVALUE_KEY, RawLumifyProperties.TEXT.getKey());
    }

    private String extractText(InputStream in, String mimeType, Metadata metadata) throws IOException, SAXException, TikaException, BoilerpipeProcessingException {
        Parser parser = new AutoDetectParser(); // TODO: the content type should already be detected. To speed this up we should be able to grab the parser from content type.
        ParseContext ctx = new ParseContext();
        String text;

        //todo use a stream that supports reset rather than copying data to memory
        byte[] input = IOUtils.toByteArray(in);
        // since we are using the AutoDetectParser, it is safe to assume that
        //the Content-Type metadata key will always return a value
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Charset charset = Charset.forName("UTF-8");
        Writer writer = new OutputStreamWriter(out, charset);
        ContentHandler handler = new BodyContentHandler(writer);
        parser.parse(new ByteArrayInputStream(input), handler, metadata, ctx);
        String bodyContent = new String(out.toByteArray(), charset);

        if (isHtml(mimeType)) {
            text = extractTextFromHtml(IOUtils.toString(new ByteArrayInputStream(input)));
            if (text == null || text.length() == 0) {
                text = cleanExtractedText(bodyContent);
            }
        } else {
            text = cleanExtractedText(bodyContent);
        }
        return text;
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

    private String cleanExtractedText(String extractedText) {
        StringBuilder trimmedText = new StringBuilder();
        String[] splitResults = extractedText.split("\\n");
        Pattern p = Pattern.compile("[a-zA-Z0-9]");
        for (int i = 0; i < splitResults.length; i++) {
            if (p.matcher(splitResults[i]).find()) {
                trimmedText.append(splitResults[i].replaceAll("\\s{2,}", " ").trim());
                if (i != splitResults.length) {
                    trimmedText.append("\n");
                }
            }
        }
        return trimmedText.toString().replaceAll("\\n{3,}", "\n\n");
    }

    @Override
    public boolean isHandled(Vertex vertex, Property property) {
        if (!property.getName().equals(RawLumifyProperties.RAW.getKey())) {
            return false;
        }

        String mimeType = (String) property.getMetadata().get(RawLumifyProperties.METADATA_MIME_TYPE);
        if (mimeType == null) {
            return false;
        }

        if (mimeType.startsWith("image") || mimeType.startsWith("video") || mimeType.startsWith("audio")) {
            return false;
        }

        return true;
    }
}
