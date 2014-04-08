package com.altamiracorp.lumify.storm.video;

import com.altamiracorp.lumify.core.ingest.ContentTypeSorter;
import org.apache.commons.compress.archivers.ArchiveEntry;

import java.io.InputStream;

public class VideoContentTypeSorter implements ContentTypeSorter {
    public static final String QUEUE_NAME = "video";
    public static final String YOUTUBE_CC_FILE_NAME_SUFFIX = ".youtubecc";
    public static final String SRT_CC_FILE_NAME_SUFFIX = ".srt";

    @Override
    public String getQueueNameFromMimeType(String mimeType) {
        mimeType = mimeType.toLowerCase();

        if (mimeType.contains("video")) {
            return QUEUE_NAME;
        }

        return null;
    }

    @Override
    public String getQueueNameFromArchiveEntry(ArchiveEntry entry, InputStream inputStream) {
        if (entry.getName().endsWith(SRT_CC_FILE_NAME_SUFFIX)
                || entry.getName().endsWith(YOUTUBE_CC_FILE_NAME_SUFFIX)) {
            return QUEUE_NAME;
        }
        return null;
    }
}
