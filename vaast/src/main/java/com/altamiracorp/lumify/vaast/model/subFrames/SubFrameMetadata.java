package com.altamiracorp.lumify.vaast.model.subFrames;

import com.altamiracorp.lumify.model.Value;
import com.altamiracorp.lumify.core.model.videoFrames.VideoFrameMetadata;

public class SubFrameMetadata extends VideoFrameMetadata{

    private static final String X1 = "x1";
    private static final String Y1 = "y1";
    private static final String X2 = "x2";
    private static final String Y2 = "y2";
    private static final String SPARSE_HDFS_PATH = "sparse_hdfs_path";

    public int getX1 () {
        return Value.toInteger(get(X1));
    }

    public SubFrameMetadata setX1 (int x1) {
        set(X1,x1);
        return this;
    }

    public int getY1 () {
        return Value.toInteger(get(Y1));
    }

    public SubFrameMetadata setY1 (int y1) {
        set(Y1,y1);
        return this;
    }

    public int getX2 () {
        return Value.toInteger(get(X2));
    }

    public SubFrameMetadata setX2 (int x2) {
        set(X2,x2);
        return this;
    }

    public int getY2 () {
        return Value.toInteger(get(Y2));
    }

    public SubFrameMetadata setY2 (int y2) {
        set(Y2,y2);
        return this;
    }

    public String getSparseHdfsPath () {
        return Value.toString(get(SPARSE_HDFS_PATH));
    }

    public SubFrameMetadata setSparseHdfsPath (String sparseHdfsPath) {
        set(SPARSE_HDFS_PATH,sparseHdfsPath);
        return this;
    }

}
