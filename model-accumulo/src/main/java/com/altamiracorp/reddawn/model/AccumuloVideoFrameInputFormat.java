package com.altamiracorp.reddawn.model;

import com.altamiracorp.reddawn.model.videoFrames.VideoFrame;
import com.altamiracorp.reddawn.model.videoFrames.VideoFrameRepository;
import org.apache.accumulo.core.client.mapreduce.AccumuloInputFormat;
import org.apache.accumulo.core.security.Authorizations;
import org.apache.hadoop.mapreduce.Job;

public class AccumuloVideoFrameInputFormat extends AccumuloBaseInputFormat<VideoFrame, VideoFrameRepository> {
    private VideoFrameRepository videoFrameRepository = new VideoFrameRepository();

    public static void init(Job job, String username, byte[] password, Authorizations authorizations, String zookeeperInstanceName, String zookeeperServerNames) {
        AccumuloInputFormat.setZooKeeperInstance(job.getConfiguration(), zookeeperInstanceName, zookeeperServerNames);
        AccumuloInputFormat.setInputInfo(job.getConfiguration(), username, password, VideoFrame.TABLE_NAME, authorizations);
    }

    @Override
    public VideoFrameRepository getRepository() {
        return videoFrameRepository;
    }
}
