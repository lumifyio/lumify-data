package com.altamiracorp.lumify.objectDetection;

import com.altamiracorp.lumify.util.OpenCVUtils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Rect;
import org.opencv.objdetect.CascadeClassifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class OpenCVObjectDetector extends ObjectDetector {

    private static final Logger LOGGER = LoggerFactory.getLogger(OpenCVObjectDetector.class);

    private static final String MODEL = "opencv";

    private CascadeClassifier objectClassifier;

    @Override
    public void setup(String classifierPath, InputStream dictionary) {
        LOGGER.warn("There is no dictionary needed for the standard OpenCV object detector, use OpenCVObjectDetector.setup(String) instead");
        setup(classifierPath);
    }

    @Override
    public void setup(String classifierPath) {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        objectClassifier = new CascadeClassifier(classifierPath);
    }

    @Override
    protected List<DetectedObject> detectObjects (BufferedImage bImage) {
        ArrayList<DetectedObject> detectedObjectList = new ArrayList<DetectedObject>();
        Mat image = OpenCVUtils.bufferedImageToMat(bImage);
        if (image != null){
            MatOfRect faceDetections = new MatOfRect();
            objectClassifier.detectMultiScale(image, faceDetections);

            for (Rect rect : faceDetections.toArray()) {
                DetectedObject detectedObject = new DetectedObject(Integer.toString(rect.x),Integer.toString(rect.y),
                        Integer.toString(rect.x + rect.width), Integer.toString(rect.y + rect.height));
                detectedObjectList.add(detectedObject);
            }
        }

        return detectedObjectList;
    }

    @Override
    public String getModelName() {
        return MODEL;
    }

}
