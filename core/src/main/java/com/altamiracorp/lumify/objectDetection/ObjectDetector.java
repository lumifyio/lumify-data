package com.altamiracorp.lumify.objectDetection;

import com.altamiracorp.lumify.AppSession;
import com.altamiracorp.lumify.model.videoFrames.VideoFrame;
import com.altamiracorp.lumify.model.videoFrames.VideoFrameRepository;
import com.altamiracorp.lumify.ucd.artifact.Artifact;
import com.altamiracorp.lumify.ucd.artifact.ArtifactRepository;

import java.awt.image.BufferedImage;
import java.util.List;

public abstract class ObjectDetector {

    public static final String MODEL = "opencv";

    private ArtifactRepository artifactRepository = new ArtifactRepository();
    private VideoFrameRepository videoFrameRepository = new VideoFrameRepository();

    public List<DetectedObject> detectObjects (AppSession session, Artifact artifact, String classifierPath) {
        BufferedImage bImage = artifactRepository.getRawAsImage(session.getModelSession(),artifact);
        return detectObjects(bImage,classifierPath);
    }

    public List<DetectedObject> detectObjects (AppSession session, VideoFrame videoFrame, String classifierPath) {
        BufferedImage bImage = videoFrameRepository.loadImage(session.getModelSession(),videoFrame);
        return detectObjects(bImage,classifierPath);
    }

    protected abstract List<DetectedObject> detectObjects (BufferedImage bImage, String classifierPath);


}
