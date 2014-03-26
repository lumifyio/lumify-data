package com.altamiracorp.lumify.storm.structuredData;

import com.altamiracorp.lumify.core.contentType.ContentTypeSorter;
import org.apache.commons.compress.archivers.ArchiveEntry;

import java.io.InputStream;

public class StructuredDataContentTypeSorter implements ContentTypeSorter {
    public static final String QUEUE_NAME = "structuredData";
    public static final String MAPPING_JSON_FILE_NAME_SUFFIX = ".mapping.json";

    @Override
    public String getQueueNameFromMimeType(String mimeType) {
        return null;
    }

    @Override
    public String getQueueNameFromArchiveEntry(ArchiveEntry entry, InputStream inputStream) {
        if (entry.getName().endsWith(MAPPING_JSON_FILE_NAME_SUFFIX)) {
            return QUEUE_NAME;
        }
        return null;
    }
}
