package com.altamiracorp.reddawn.textExtraction;

import com.altamiracorp.reddawn.model.Session;
import com.altamiracorp.reddawn.model.videoFrames.VideoFrame;
import com.altamiracorp.reddawn.ucd.artifact.Artifact;
import com.altamiracorp.reddawn.ucd.artifact.VideoTranscript;
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
