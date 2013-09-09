package com.altamiracorp.lumify.textExtraction;

import com.altamiracorp.lumify.model.Session;
import com.altamiracorp.lumify.model.videoFrames.VideoFrame;
import com.altamiracorp.lumify.ucd.artifact.Artifact;
import com.altamiracorp.lumify.ucd.artifact.VideoTranscript;
import org.apache.hadoop.mapreduce.Mapper;

public class TranscriptTextExtractor implements TextExtractor {
    private static final String NAME = "transcriptTextExtractor";

    @Override
    public void setup(Mapper.Context context) {
    }

    @Override
    public ArtifactExtractedInfo extract(Session session, Artifact artifact) throws Exception {
        VideoTranscript videoTranscript = artifact.getContent().getVideoTranscript();
        if (videoTranscript == null) {
            return null;
        }

        ArtifactExtractedInfo extractedInfo = new ArtifactExtractedInfo();
        extractedInfo.setText(videoTranscript.toString());
        return extractedInfo;
    }

    @Override
    public VideoFrameExtractedInfo extract(Session session, VideoFrame videoFrame) throws Exception {
        return null;
    }

    @Override
    public String getName() {
        return NAME;
    }

}
