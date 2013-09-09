package com.altamiracorp.lumify.textExtraction;

import com.altamiracorp.lumify.model.Session;
import com.altamiracorp.lumify.model.videoFrames.VideoFrame;
import com.altamiracorp.lumify.ucd.artifact.Artifact;
import org.apache.hadoop.mapreduce.Mapper;

public interface TextExtractor {
    void setup(Mapper.Context context) throws Exception;

    ArtifactExtractedInfo extract(Session session, Artifact artifact) throws Exception;

    VideoFrameExtractedInfo extract(Session session, VideoFrame videoFrame) throws Exception;

    String getName();
}
