package com.altamiracorp.lumify.objectDetection;

import com.altamiracorp.lumify.AppSession;
import com.altamiracorp.lumify.model.videoFrames.VideoFrame;
import com.altamiracorp.lumify.model.videoFrames.VideoFrameRepository;
import com.altamiracorp.lumify.ucd.artifact.Artifact;
import com.altamiracorp.lumify.ucd.artifact.ArtifactRepository;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public abstract class ObjectDetector {

    private ArtifactRepository artifactRepository = new ArtifactRepository();
    private VideoFrameRepository videoFrameRepository = new VideoFrameRepository();

    public List<DetectedObject> detectObjects (AppSession session, Artifact artifact) throws IOException{
        BufferedImage bImage = artifactRepository.getRawAsImage(session.getModelSession(),artifact);
        return detectObjects(bImage);
    }

    public List<DetectedObject> detectObjects (AppSession session, VideoFrame videoFrame) throws IOException{
        BufferedImage bImage = videoFrameRepository.loadImage(session.getModelSession(),videoFrame);
        return detectObjects(bImage);
    }

    public abstract void setup (String classifierPath, InputStream dictionary) throws IOException;

    public abstract void setup (String classifierPath) throws IOException;

    protected abstract List<DetectedObject> detectObjects (BufferedImage bImage) throws IOException;

    public abstract String getModelName ();


}
