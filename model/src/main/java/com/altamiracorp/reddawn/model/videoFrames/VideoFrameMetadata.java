package com.altamiracorp.reddawn.model.videoFrames;

import com.altamiracorp.reddawn.model.ColumnFamily;
import com.altamiracorp.reddawn.model.Value;

public class VideoFrameMetadata extends ColumnFamily {
    public static final String NAME = "metadata";
    private static final String HDFS_PATH = "hdfs_path";

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
}
