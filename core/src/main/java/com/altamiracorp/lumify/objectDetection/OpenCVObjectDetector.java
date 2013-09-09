package com.altamiracorp.lumify.objectDetection;

import com.altamiracorp.lumify.util.OpenCVUtils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Rect;
import org.opencv.objdetect.CascadeClassifier;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public class OpenCVObjectDetector extends ObjectDetector {

    @Override
    protected List<DetectedObject> detectObjects (BufferedImage bImage, String classifierPath) {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        ArrayList<DetectedObject> detectedObjectList = new ArrayList<DetectedObject>();
        Mat image = OpenCVUtils.bufferedImageToMat(bImage);
        if (image != null){
            CascadeClassifier faceDetector = new CascadeClassifier(classifierPath);

            MatOfRect faceDetections = new MatOfRect();
            faceDetector.detectMultiScale(image, faceDetections);

            for (Rect rect : faceDetections.toArray()) {
                DetectedObject detectedObject = new DetectedObject(Integer.toString(rect.x),Integer.toString(rect.y),
                        Integer.toString(rect.x + rect.width), Integer.toString(rect.y + rect.height));
                detectedObjectList.add(detectedObject);
            }
        }

        return detectedObjectList;
    }

}
