package com.altamiracorp.lumify.textExtraction;

import com.altamiracorp.lumify.model.ModelSession;
import com.altamiracorp.lumify.model.videoFrames.VideoFrame;
import com.altamiracorp.lumify.ucd.artifact.Artifact;
import org.apache.hadoop.mapreduce.Mapper;

public interface TextExtractor {
    void setup(Mapper.Context context) throws Exception;

    ArtifactExtractedInfo extract(ModelSession session, Artifact artifact) throws Exception;

    VideoFrameExtractedInfo extract(ModelSession session, VideoFrame videoFrame) throws Exception;

    String getName();
}
