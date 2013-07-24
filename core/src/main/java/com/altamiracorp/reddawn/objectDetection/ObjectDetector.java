package com.altamiracorp.reddawn.objectDetection;

import com.altamiracorp.reddawn.RedDawnSession;
import com.altamiracorp.reddawn.model.videoFrames.VideoFrame;
import com.altamiracorp.reddawn.model.videoFrames.VideoFrameRepository;
import com.altamiracorp.reddawn.ucd.artifact.Artifact;
import com.altamiracorp.reddawn.ucd.artifact.ArtifactRepository;

import static com.googlecode.javacv.cpp.opencv_core.*;
import static com.googlecode.javacv.cpp.opencv_imgproc.*;
import static com.googlecode.javacv.cpp.opencv_objdetect.*;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public class ObjectDetector {

    public static final String MODEL = "opencv";

    private ArtifactRepository artifactRepository = new ArtifactRepository();
    private VideoFrameRepository videoFrameRepository = new VideoFrameRepository();

    public List<DetectedObject> detectObjects (RedDawnSession session, Artifact artifact, String classifierPath) {
        BufferedImage bImage = artifactRepository.getRawAsImage(session.getModelSession(),artifact);
        return detectObjects(bImage,classifierPath);
    }

    public List<DetectedObject> detectObjects (RedDawnSession session, VideoFrame videoFrame, String classifierPath) {
        BufferedImage bImage = videoFrameRepository.loadImage(session.getModelSession(),videoFrame);
        return detectObjects(bImage,classifierPath);
    }

    private List<DetectedObject> detectObjects (BufferedImage bImage, String classifierPath) {
        ArrayList<DetectedObject> detectedObjectList = new ArrayList<DetectedObject>();
        IplImage image = IplImage.createFrom(bImage);

        IplImage grayImage = IplImage.create(image.width(),image.height(),IPL_DEPTH_8U,1);
        cvCvtColor(image, grayImage, CV_BGR2GRAY);

        CvHaarClassifierCascade classifier = new CvHaarClassifierCascade(cvLoad(classifierPath));

        CvMemStorage storage = CvMemStorage.create();

        // Let's try to detect some faces! but we need a grayscale image...
        CvSeq faces = cvHaarDetectObjects(grayImage, classifier, storage,
                1.1, 3, CV_HAAR_DO_CANNY_PRUNING);
        int total = faces.total();
        for (int i = 0; i < total; i++) {
            CvRect r = new CvRect(cvGetSeqElem(faces, i));
            int x = r.x(), y = r.y(), w = r.width(), h = r.height();
            DetectedObject detectedObject = new DetectedObject(x,y,x + w, y + h);
            detectedObjectList.add(detectedObject);
        }
        cvClearMemStorage(storage);

        return detectedObjectList;
    }


}
