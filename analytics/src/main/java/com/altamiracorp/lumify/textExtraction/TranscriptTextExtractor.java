package com.altamiracorp.lumify.textExtraction;

import com.altamiracorp.lumify.core.ingest.ArtifactExtractedInfo;
import com.altamiracorp.lumify.core.user.User;
import com.altamiracorp.lumify.core.model.videoFrames.VideoFrame;
import com.altamiracorp.lumify.core.model.artifact.Artifact;
import com.google.inject.Injector;
import org.apache.hadoop.mapreduce.Mapper;

public class TranscriptTextExtractor implements TextExtractor {
    private static final String NAME = "transcriptTextExtractor";

    @Override
    public void setup(Mapper.Context context, Injector injector) {
    }

    @Override
    public ArtifactExtractedInfo extract(Artifact artifact, User user) throws Exception {
        throw new RuntimeException("storm refactor - not implemented"); // TODO storm refactor
//        VideoTranscript videoTranscript = artifact.getContent().getVideoTranscript();
//        if (videoTranscript == null) {
//            return null;
//        }
//
//        ArtifactExtractedInfo extractedInfo = new ArtifactExtractedInfo();
//        extractedInfo.setText(videoTranscript.toString());
//        return extractedInfo;
    }

    @Override
    public VideoFrameExtractedInfo extract(VideoFrame videoFrame, User user) throws Exception {
        return null;
    }

    @Override
    public String getName() {
        return NAME;
    }

}
