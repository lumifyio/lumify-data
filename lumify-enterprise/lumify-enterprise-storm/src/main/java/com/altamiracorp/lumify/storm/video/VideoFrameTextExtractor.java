package com.altamiracorp.lumify.storm.video;

import com.altamiracorp.lumify.core.ingest.AdditionalArtifactWorkData;
import com.altamiracorp.lumify.core.ingest.ArtifactExtractedInfo;
import com.altamiracorp.lumify.core.util.LumifyLogger;
import com.altamiracorp.lumify.core.util.LumifyLoggerFactory;
import com.altamiracorp.lumify.textExtraction.ImageOcrTextExtractor;
import com.altamiracorp.lumify.textExtraction.VideoFrameExtractedInfo;
import com.google.inject.Inject;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;

import javax.imageio.ImageIO;
import java.util.List;

public class VideoFrameTextExtractor {
    private ImageOcrTextExtractor imageOcrTextExtractor;
    private static final LumifyLogger LOGGER = LumifyLoggerFactory.getLogger(VideoFrameTextExtractor.class);

    public ArtifactExtractedInfo extract(List<ArtifactExtractedInfo.VideoFrame> videoFrames, AdditionalArtifactWorkData data) throws Exception {
        LOGGER.debug("Extracting Frame Text [VideoFrameTextExtractor]: %s", data.getFileName());
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
        LOGGER.debug("Finished [VideoFrameTextExtractor]: %s", data.getFileName());
        return info;
    }

    @Inject
    public void setImageOcrTextExtractor(ImageOcrTextExtractor imageOcrTextExtractor) {
        this.imageOcrTextExtractor = imageOcrTextExtractor;
    }

}
