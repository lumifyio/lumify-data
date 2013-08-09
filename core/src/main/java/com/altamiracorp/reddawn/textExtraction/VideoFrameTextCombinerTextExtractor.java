package com.altamiracorp.reddawn.textExtraction;

import com.altamiracorp.reddawn.model.Session;
import com.altamiracorp.reddawn.model.videoFrames.VideoFrame;
import com.altamiracorp.reddawn.model.videoFrames.VideoFrameRepository;
import com.altamiracorp.reddawn.ucd.artifact.Artifact;
import com.altamiracorp.reddawn.ucd.artifact.ArtifactType;
import com.altamiracorp.reddawn.ucd.artifact.VideoTranscript;
import org.apache.hadoop.mapreduce.Mapper;

import java.util.List;

public class VideoFrameTextCombinerTextExtractor implements TextExtractor {
    private static final String NAME = "videoFrameTextCombinerExtractor";

    private VideoFrameRepository videoFrameRepository = new VideoFrameRepository();

    @Override
    public void setup(Mapper.Context context) {
    }

    @Override
    public ArtifactExtractedInfo extract(Session session, Artifact artifact) throws Exception {
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
            if (time != null && text != null) {
                transcript.add(time, text);
            }
        }

        ArtifactExtractedInfo extractedInfo = new ArtifactExtractedInfo();
        extractedInfo.setText(transcript.toString());
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
