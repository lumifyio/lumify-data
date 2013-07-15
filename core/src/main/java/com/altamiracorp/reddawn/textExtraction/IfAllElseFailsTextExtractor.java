package com.altamiracorp.reddawn.textExtraction;

import com.altamiracorp.reddawn.model.Session;
import com.altamiracorp.reddawn.model.videoFrames.VideoFrame;
import com.altamiracorp.reddawn.ucd.artifact.Artifact;
import org.apache.hadoop.mapreduce.Mapper;

public class IfAllElseFailsTextExtractor implements TextExtractor {
    @Override
    public void setup(Mapper.Context context) {
    }

    @Override
    public ArtifactExtractedInfo extract(Session session, Artifact artifact) throws Exception {
        ArtifactExtractedInfo extractedInfo = new ArtifactExtractedInfo();

        if (!hasExtractedText(artifact)) {
            extractedInfo.setText(artifact.getGenericMetadata().getFileName() + "." + artifact.getGenericMetadata().getFileExtension());
        }

        return extractedInfo;
    }

    private boolean hasExtractedText(Artifact artifact) {
        byte[] text = artifact.getContent().getDocExtractedText();
        if (text != null && text.length > 0) {
            return true;
        }

        String hdfsPath = artifact.getGenericMetadata().getExtractedTextHdfsPath();
        if (hdfsPath != null && hdfsPath.trim().length() > 0) {
            return true;
        }

        return false;
    }

    @Override
    public VideoFrameExtractedInfo extract(Session session, VideoFrame videoFrame) throws Exception {
        return null;
    }

}
