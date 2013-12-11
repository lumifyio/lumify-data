package com.altamiracorp.lumify.objectDetection;

import com.altamiracorp.lumify.core.ingest.ArtifactDetectedObject;
import com.altamiracorp.lumify.core.model.videoFrames.VideoFrame;
import com.altamiracorp.lumify.core.model.videoFrames.VideoFrameRepository;
import com.altamiracorp.lumify.core.user.User;
import com.google.inject.Inject;
import org.apache.hadoop.fs.FileSystem;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public abstract class ObjectDetector {
    private VideoFrameRepository videoFrameRepository;

    public List<ArtifactDetectedObject> detectObjects(VideoFrame videoFrame, User user) throws IOException {
        BufferedImage bImage = videoFrameRepository.loadImage(videoFrame, user);
        return detectObjects(bImage);
    }

    public abstract List<ArtifactDetectedObject> detectObjects(BufferedImage bImage) throws IOException;

    public abstract String getModelName();

    public void init(Map map, FileSystem hdfsFileSystem) throws Exception {

    }

    @Inject
    public void setVideoFrameRepository(VideoFrameRepository videoFrameRepository) {
        this.videoFrameRepository = videoFrameRepository;
    }

    public VideoFrameRepository getVideoFrameRepository() {
        return videoFrameRepository;
    }
}
