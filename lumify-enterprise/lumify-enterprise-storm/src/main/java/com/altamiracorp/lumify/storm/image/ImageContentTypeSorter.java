package com.altamiracorp.lumify.storm.image;

import com.altamiracorp.lumify.core.contentType.ContentTypeSorter;
import org.apache.commons.compress.archivers.ArchiveEntry;

import java.io.InputStream;

public class ImageContentTypeSorter implements ContentTypeSorter {
    @Override
    public String getQueueNameFromMimeType(String mimeType) {
        mimeType = mimeType.toLowerCase();

        if (mimeType.contains("image")) {
            return "image";
        }

        return null;
    }

    @Override
    public String getQueueNameFromArchiveEntry(ArchiveEntry entry, InputStream inputStream) {
        return null;
    }
}
