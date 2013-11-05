package com.altamiracorp.lumify.storm.video;

import com.altamiracorp.lumify.core.ingest.AdditionalArtifactWorkData;
import com.altamiracorp.lumify.core.ingest.ArtifactExtractedInfo;
import com.altamiracorp.lumify.textExtraction.ImageOcrTextExtractor;
import com.altamiracorp.lumify.textExtraction.VideoFrameExtractedInfo;
import com.google.inject.Inject;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;

import javax.imageio.ImageIO;
import java.util.List;

public class VideoFrameTextExtractor {
    private ImageOcrTextExtractor imageOcrTextExtractor;

    public ArtifactExtractedInfo extract(List<ArtifactExtractedInfo.VideoFrame> videoFrames, AdditionalArtifactWorkData data) throws Exception {
        ArtifactExtractedInfo info = new ArtifactExtractedInfo();
        StringBuilder builder = new StringBuilder();
        FileSystem fs = data.getHdfsFileSystem();
        for (ArtifactExtractedInfo.VideoFrame frame : videoFrames) {
            VideoFrameExtractedInfo frameInfo = imageOcrTextExtractor.extractFromVideoFrame(
                    ImageIO.read(fs.open(new Path(frame.getHdfsPath()))), data.getMimeType());
            if (frameInfo == null) {
                continue;
            }
            builder.append(frameInfo.getText());
            builder.append("\n");
        }
        info.setText(builder.toString());
        return info;
    }

    @Inject
    public void setImageOcrTextExtractor(ImageOcrTextExtractor imageOcrTextExtractor) {
        this.imageOcrTextExtractor = imageOcrTextExtractor;
    }

}
