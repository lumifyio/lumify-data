package com.altamiracorp.reddawn.textExtraction;

import com.altamiracorp.reddawn.model.Session;
import com.altamiracorp.reddawn.model.videoFrames.VideoFrame;
import com.altamiracorp.reddawn.ucd.artifact.Artifact;
import org.apache.hadoop.mapreduce.Mapper;

public interface TextExtractor {
    void setup(Mapper.Context context);

    ArtifactExtractedInfo extract(Session session, Artifact artifact) throws Exception;

    VideoFrameExtractedInfo extract(Session session, VideoFrame videoFrame) throws Exception;
}
