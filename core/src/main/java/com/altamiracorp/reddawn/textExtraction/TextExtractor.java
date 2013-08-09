package com.altamiracorp.reddawn.textExtraction;

import com.altamiracorp.reddawn.model.Session;
import com.altamiracorp.reddawn.model.videoFrames.VideoFrame;
import com.altamiracorp.reddawn.ucd.artifact.Artifact;
import org.apache.hadoop.mapreduce.Mapper;

import javax.xml.parsers.ParserConfigurationException;

public interface TextExtractor {
    void setup(Mapper.Context context) throws Exception;

    ArtifactExtractedInfo extract(Session session, Artifact artifact) throws Exception;

    VideoFrameExtractedInfo extract(Session session, VideoFrame videoFrame) throws Exception;

    String getName();
}
