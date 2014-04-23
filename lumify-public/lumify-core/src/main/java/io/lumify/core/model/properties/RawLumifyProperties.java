package io.lumify.core.model.properties;

import io.lumify.core.model.properties.types.DateLumifyProperty;
import io.lumify.core.model.properties.types.StreamingLumifyProperty;
import io.lumify.core.model.properties.types.TextLumifyProperty;
import org.securegraph.TextIndexHint;

/**
 * LumifyProperties specific to Raw entities (e.g. documents, images, video, etc.).
 */
public class RawLumifyProperties {
    public static final String METADATA_MIME_TYPE = "http://lumify.io#mimeType";

    public static final DateLumifyProperty PUBLISHED_DATE = new DateLumifyProperty("http://lumify.io#publishedDate");
    public static final DateLumifyProperty CREATE_DATE = new DateLumifyProperty("http://lumify.io#createDate");
    public static final TextLumifyProperty FILE_NAME = TextLumifyProperty.all("http://lumify.io#fileName");
    public static final TextLumifyProperty FILE_NAME_EXTENSION = new TextLumifyProperty("http://lumify.io#fileNameExtension", TextIndexHint.EXACT_MATCH);
    public static final TextLumifyProperty MIME_TYPE = TextLumifyProperty.all(METADATA_MIME_TYPE);
    public static final TextLumifyProperty AUTHOR = TextLumifyProperty.all("http://lumify.io#author");
    public static final StreamingLumifyProperty RAW = new StreamingLumifyProperty("http://lumify.io#raw");
    public static final StreamingLumifyProperty TEXT = new StreamingLumifyProperty("http://lumify.io#text");

    private RawLumifyProperties() {
        throw new UnsupportedOperationException("do not construct utility class");
    }
}
