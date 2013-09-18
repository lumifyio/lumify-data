package com.altamiracorp.lumify.textExtraction;

import com.altamiracorp.lumify.model.ModelSession;
import com.altamiracorp.lumify.model.videoFrames.VideoFrame;
import com.altamiracorp.lumify.model.videoFrames.VideoFrameRepository;
import com.altamiracorp.lumify.ucd.artifact.Artifact;
import com.altamiracorp.lumify.ucd.artifact.ArtifactType;
import com.altamiracorp.lumify.ucd.artifact.VideoTranscript;
import org.apache.hadoop.mapreduce.Mapper;

import java.util.List;

public class VideoFrameTextCombinerTextExtractor implements TextExtractor {
    private static final String NAME = "videoFrameTextCombinerExtractor";

    private VideoFrameRepository videoFrameRepository = new VideoFrameRepository();

    @Override
    public void setup(Mapper.Context context) {
    }

    @Override
    public ArtifactExtractedInfo extract(ModelSession session, Artifact artifact) throws Exception {
        if (artifact.getType() != ArtifactType.VIDEO) {
            return null;
        }

        VideoTranscript transcript = artifact.getContent().getVideoTranscript();
        if (transcript == null) {
            transcript = new VideoTranscript();
        }
        List<VideoFrame> videoFrames = videoFrameRepository.findAllByArtifactRowKey(session, artifact.getRowKey().toString());
        for (VideoFrame videoFrame : videoFrames) {
            VideoTranscript.Time time = new VideoTranscript.Time(videoFrame.getRowKey().getTime(), null);
            String text = videoFrame.getMetadata().getText();
            if (text != null) {
                transcript.add(time, text);
            }
        }

        artifact.getContent().mergeVideoTranscript(transcript);

        ArtifactExtractedInfo extractedInfo = new ArtifactExtractedInfo();
        extractedInfo.setText(transcript.toString());
        return extractedInfo;
    }

    @Override
    public VideoFrameExtractedInfo extract(ModelSession session, VideoFrame videoFrame) throws Exception {
        return null;
    }

    @Override
    public String getName() {
        return NAME;
    }


}
