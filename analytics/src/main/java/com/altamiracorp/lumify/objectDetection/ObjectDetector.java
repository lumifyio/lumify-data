package com.altamiracorp.lumify.objectDetection;

import com.altamiracorp.lumify.core.ingest.ArtifactDetectedObject;
import com.altamiracorp.lumify.core.user.User;
import com.altamiracorp.lumify.model.videoFrames.VideoFrame;
import com.altamiracorp.lumify.model.videoFrames.VideoFrameRepository;
import com.altamiracorp.lumify.ucd.artifact.Artifact;
import com.altamiracorp.lumify.ucd.artifact.ArtifactRepository;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public abstract class ObjectDetector {
    private final ArtifactRepository artifactRepository;
    private final VideoFrameRepository videoFrameRepository;

    public ObjectDetector(ArtifactRepository artifactRepository, VideoFrameRepository videoFrameRepository) {
        this.artifactRepository = artifactRepository;
        this.videoFrameRepository = videoFrameRepository;
    }

    public List<ArtifactDetectedObject> detectObjects(Artifact artifact, User user) throws IOException {
        BufferedImage bImage = artifactRepository.getRawAsImage(artifact, user);
        return detectObjects(bImage);
    }

    public List<ArtifactDetectedObject> detectObjects(VideoFrame videoFrame, User user) throws IOException {
        BufferedImage bImage = videoFrameRepository.loadImage(videoFrame, user);
        return detectObjects(bImage);
    }

    public abstract void setup(String classifierPath, InputStream dictionary) throws IOException;

    public abstract void setup(String classifierPath) throws IOException;

    public abstract List<ArtifactDetectedObject> detectObjects(BufferedImage bImage) throws IOException;

    public abstract String getModelName();


}
