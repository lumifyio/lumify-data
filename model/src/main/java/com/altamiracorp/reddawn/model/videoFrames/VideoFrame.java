package com.altamiracorp.reddawn.model.videoFrames;

import com.altamiracorp.reddawn.model.Row;
import com.altamiracorp.reddawn.model.RowKey;
import com.altamiracorp.reddawn.ucd.artifact.VideoTranscript;

public class VideoFrame extends Row<VideoFrameRowKey> {
    public static final String TABLE_NAME = "atc_VideoFrame";

    public VideoFrame(VideoFrameRowKey rowKey) {
        super(TABLE_NAME, rowKey);
    }

    public VideoFrame(RowKey rowKey) {
        super(TABLE_NAME, new VideoFrameRowKey(rowKey.toString()));
    }

    public VideoFrameMetadata getMetadata() {
        VideoFrameMetadata videoFrameMetadata = get(VideoFrameMetadata.NAME);
        if (videoFrameMetadata == null) {
            addColumnFamily(new VideoFrameMetadata());
        }
        return get(VideoFrameMetadata.NAME);
    }

    public VideoFrameDetectedObjects getDetectedObjects() {
        VideoFrameDetectedObjects videoFrameDetectedObjects = get(VideoFrameDetectedObjects.NAME);
        if (videoFrameDetectedObjects == null) {
            addColumnFamily(new VideoFrameDetectedObjects());
        }
        return get(VideoFrameDetectedObjects.NAME);
    }
}
