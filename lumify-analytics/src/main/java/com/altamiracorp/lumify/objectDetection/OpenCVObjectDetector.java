package com.altamiracorp.lumify.objectDetection;

import com.altamiracorp.lumify.core.ingest.ArtifactDetectedObject;
import com.altamiracorp.lumify.core.util.LumifyLogger;
import com.altamiracorp.lumify.core.util.LumifyLoggerFactory;
import com.altamiracorp.lumify.util.OpenCVUtils;
import org.apache.commons.io.IOUtils;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Rect;
import org.opencv.objdetect.CascadeClassifier;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;

public class OpenCVObjectDetector extends ObjectDetector {
    private static final LumifyLogger LOGGER = LumifyLoggerFactory.getLogger(OpenCVObjectDetector.class);
    public static final String OPENCV_CLASSIFIER_CONCEPT_LIST = "objectdetection.classifierConcepts";
    public static final String OPENCV_CLASSIFIER_PATH_PREFIX = "objectdetection.classifier.";
    public static final String OPENCV_CLASSIFIER_PATH_SUFFIX = ".path";
    public static final String OPENCV_DISABLED = "objectdetection.opencv.disabled";

    private static final String MODEL = "opencv";

    private List<CascadeClassifierHolder> objectClassifiers = new ArrayList<CascadeClassifierHolder>();

    @Override
    public void init(Map stormConf, FileSystem fs) throws Exception {
        super.init(stormConf, fs);
        String opencvDisabled = (String) stormConf.get(OPENCV_DISABLED);
        if (opencvDisabled != null && opencvDisabled.equals("true")) {
            return;
        }

        loadLibrary();

        String conceptListString = (String) stormConf.get(OPENCV_CLASSIFIER_CONCEPT_LIST);
        checkNotNull(conceptListString, OPENCV_CLASSIFIER_CONCEPT_LIST + " is a required configuration parameter");
        String[] classifierConcepts = conceptListString.split(",");
        for (String classifierConcept : classifierConcepts) {
            String classifierFilePath = (String) stormConf.get(OPENCV_CLASSIFIER_PATH_PREFIX + classifierConcept + OPENCV_CLASSIFIER_PATH_SUFFIX);

            File localFile = createLocalFile(classifierFilePath, fs);
            CascadeClassifier objectClassifier = new CascadeClassifier(localFile.getPath());
            addObjectClassifier(classifierConcept, objectClassifier);
            localFile.delete();
        }
    }

    public void loadLibrary () {
        try {
            System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        } catch (UnsatisfiedLinkError ex) {
            String javaLibraryPath = System.getProperty("java.library.path");
            throw new RuntimeException("Could not find opencv library: " + Core.NATIVE_LIBRARY_NAME + " (java.library.path: " + javaLibraryPath + ")", ex);
        }
    }

    public void addObjectClassifier(String concept, CascadeClassifier objectClassifier) {
        objectClassifiers.add(new CascadeClassifierHolder(concept, objectClassifier));
    }

    @Override
    public List<ArtifactDetectedObject> detectObjects(BufferedImage bImage) {
        ArrayList<ArtifactDetectedObject> detectedObjectList = new ArrayList<ArtifactDetectedObject>();
        Mat image = OpenCVUtils.bufferedImageToMat(bImage);
        if (image != null) {
            MatOfRect faceDetections = new MatOfRect();
            for (CascadeClassifierHolder objectClassifier : objectClassifiers) {
                objectClassifier.cascadeClassifier.detectMultiScale(image, faceDetections);

                for (Rect rect : faceDetections.toArray()) {
                    ArtifactDetectedObject detectedObject = new ArtifactDetectedObject(
                            rect.x,
                            rect.y,
                            rect.x + rect.width,
                            rect.y + rect.height,
                            objectClassifier.concept);
                    detectedObjectList.add(detectedObject);
                }
            }
        }

        return detectedObjectList;
    }

    @Override
    public String getModelName() {
        return MODEL;
    }

    private File createLocalFile(String classifierFilePath, FileSystem fs) throws IOException {
        File tempFile = File.createTempFile("lumify", ".xml");
        FileOutputStream fos = null;
        InputStream in = null;
        try {
            in = fs.open(new Path(classifierFilePath));
            fos = new FileOutputStream(tempFile);
            IOUtils.copy(in, fos);
        } catch (IOException e) {
            LOGGER.error("Could not create local file", e);
        } finally {
            if (in != null) {
                in.close();
            }
            if (fos != null) {
                fos.close();
            }
        }
        return tempFile;
    }

    private class CascadeClassifierHolder {
        public final String concept;
        public final CascadeClassifier cascadeClassifier;

        public CascadeClassifierHolder(String concept, CascadeClassifier cascadeClassifier) {
            this.concept = concept;
            this.cascadeClassifier = cascadeClassifier;
        }
    }
}
