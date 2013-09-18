package com.altamiracorp.lumify.model;

import com.altamiracorp.lumify.model.videoFrames.VideoFrame;
import com.altamiracorp.lumify.model.videoFrames.VideoFrameBuilder;
import org.apache.accumulo.core.client.mapreduce.AccumuloInputFormat;
import org.apache.accumulo.core.security.Authorizations;
import org.apache.hadoop.mapreduce.Job;

public class AccumuloVideoFrameInputFormat extends AccumuloBaseInputFormat<VideoFrame, VideoFrameBuilder> {
    private VideoFrameBuilder videoFrameBuilder = new VideoFrameBuilder();

    public static void init(Job job, String username, String password, Authorizations authorizations, String zookeeperInstanceName, String zookeeperServerNames) {
        AccumuloInputFormat.setZooKeeperInstance(job.getConfiguration(), zookeeperInstanceName, zookeeperServerNames);
        AccumuloInputFormat.setInputInfo(job.getConfiguration(), username, password.getBytes(), VideoFrame.TABLE_NAME, authorizations);
    }

    @Override
    public VideoFrameBuilder getBuilder() {
        return videoFrameBuilder;
    }
}
