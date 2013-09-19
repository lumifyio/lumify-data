package com.altamiracorp.lumify.textExtraction;

import com.altamiracorp.lumify.core.user.User;
import com.altamiracorp.lumify.model.videoFrames.VideoFrame;
import com.altamiracorp.lumify.ucd.artifact.Artifact;
import org.apache.hadoop.mapreduce.Mapper;

public interface TextExtractor {
    void setup(Mapper.Context context) throws Exception;

    ArtifactExtractedInfo extract(Artifact artifact, User user) throws Exception;

    VideoFrameExtractedInfo extract(VideoFrame videoFrame, User user) throws Exception;

    String getName();
}
