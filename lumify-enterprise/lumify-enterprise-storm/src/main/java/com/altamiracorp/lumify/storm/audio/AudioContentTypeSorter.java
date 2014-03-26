package com.altamiracorp.lumify.storm.audio;

import com.altamiracorp.lumify.core.contentType.ContentTypeSorter;
import org.apache.commons.compress.archivers.ArchiveEntry;

import java.io.InputStream;

public class AudioContentTypeSorter implements ContentTypeSorter {
    public static final String QUEUE_NAME = "audio";

    @Override
    public String getQueueNameFromMimeType(String mimeType) {
        mimeType = mimeType.toLowerCase();

        if (mimeType.contains("audio")) {
            return QUEUE_NAME;
        }

        return null;
    }

    @Override
    public String getQueueNameFromArchiveEntry(ArchiveEntry entry, InputStream inputStream) {
        return null;
    }
}
