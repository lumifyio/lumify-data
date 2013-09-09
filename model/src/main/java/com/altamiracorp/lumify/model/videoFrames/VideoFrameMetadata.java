package com.altamiracorp.lumify.model.videoFrames;

import com.altamiracorp.lumify.model.ColumnFamily;
import com.altamiracorp.lumify.model.Value;

public class VideoFrameMetadata extends ColumnFamily {
    public static final String NAME = "metadata";
    private static final String HDFS_PATH = "hdfs_path";
    private static final String TEXT = "text";
    private static final String START_TIME = "start_time";
    private static final String END_TIME = "end_time";

    public VideoFrameMetadata() {
        super(NAME);
    }

    public String getHdfsPath() {
        return Value.toString(get(HDFS_PATH));
    }

    public VideoFrameMetadata setHdfsPath(String hdfsPath) {
        set(HDFS_PATH, hdfsPath);
        return this;
    }

    public String getText() {
        return Value.toString(get(TEXT));
    }

    public VideoFrameMetadata setText(String text) {
        set(TEXT, text);
        return this;
    }
}
