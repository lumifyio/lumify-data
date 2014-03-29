package com.altamiracorp.lumify.opencvObjectDetector;

import com.altamiracorp.lumify.core.exception.LumifyException;
import com.altamiracorp.lumify.core.ingest.ArtifactDetectedObject;
import com.altamiracorp.lumify.core.ingest.graphProperty.GraphPropertyWorkData;
import com.altamiracorp.lumify.core.ingest.graphProperty.GraphPropertyWorkResult;
import com.altamiracorp.lumify.core.ingest.graphProperty.GraphPropertyWorker;
import com.altamiracorp.lumify.core.ingest.graphProperty.GraphPropertyWorkerPrepareData;
import com.altamiracorp.lumify.core.model.detectedObjects.DetectedObjectRepository;
import com.altamiracorp.lumify.core.model.properties.RawLumifyProperties;
import com.altamiracorp.lumify.core.security.LumifyVisibility;
import com.altamiracorp.lumify.core.user.User;
import com.altamiracorp.securegraph.Property;
import com.altamiracorp.securegraph.Vertex;
import com.google.inject.Inject;
import org.apache.commons.io.IOUtils;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Rect;
import org.opencv.objdetect.CascadeClassifier;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

public class OpenCVObjectDetectorPropertyWorker extends GraphPropertyWorker {
    public static final String OPENCV_CLASSIFIER_CONCEPT_LIST = "objectdetection.classifierConcepts";
    public static final String OPENCV_CLASSIFIER_PATH_PREFIX = "objectdetection.classifier.";
    public static final String OPENCV_CLASSIFIER_PATH_SUFFIX = ".path";

    private List<CascadeClassifierHolder> objectClassifiers = new ArrayList<CascadeClassifierHolder>();
    private DetectedObjectRepository detectedObjectRepository;
    private User user;

    @Override
    public void prepare(GraphPropertyWorkerPrepareData workerPrepareData) throws Exception {
        super.prepare(workerPrepareData);
        this.user = workerPrepareData.getUser();

        loadNativeLibrary();

        String conceptListString = (String) workerPrepareData.getStormConf().get(OPENCV_CLASSIFIER_CONCEPT_LIST);
        checkNotNull(conceptListString, OPENCV_CLASSIFIER_CONCEPT_LIST + " is a required configuration parameter");
        String[] classifierConcepts = conceptListString.split(",");
        for (String classifierConcept : classifierConcepts) {
            String classifierFilePath = (String) workerPrepareData.getStormConf().get(OPENCV_CLASSIFIER_PATH_PREFIX + classifierConcept + OPENCV_CLASSIFIER_PATH_SUFFIX);

            File localFile = createLocalFile(classifierFilePath, workerPrepareData.getHdfsFileSystem());
            CascadeClassifier objectClassifier = new CascadeClassifier(localFile.getPath());
            addObjectClassifier(classifierConcept, objectClassifier);
            localFile.delete();
        }
    }

    public void loadNativeLibrary() {
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

    private File createLocalFile(String classifierFilePath, FileSystem fs) throws IOException {
        File tempFile = File.createTempFile("lumify-opencv-objdetect", ".xml");
        FileOutputStream fos = null;
        InputStream in = null;
        try {
            in = fs.open(new Path(classifierFilePath));
            fos = new FileOutputStream(tempFile);
            IOUtils.copy(in, fos);
        } catch (IOException e) {
            throw new LumifyException("Could not create local file", e);
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

    @Override
    public GraphPropertyWorkResult execute(InputStream in, GraphPropertyWorkData data) throws Exception {
        BufferedImage bImage = ImageIO.read(in);

        List<ArtifactDetectedObject> detectedObjects = detectObjects(bImage);
        saveDetectedObjects(data.getVertex(), detectedObjects);

        return new GraphPropertyWorkResult();
    }

    private void saveDetectedObjects(Vertex vertex, List<ArtifactDetectedObject> detectedObjects) {
        for (ArtifactDetectedObject detectedObject : detectedObjects) {
            saveDetectedObject(vertex, detectedObject);
        }
    }

    private void saveDetectedObject(Vertex vertex, ArtifactDetectedObject detectedObject) {
        detectedObjectRepository.saveDetectedObject(
                vertex.getId(),
                null,
                detectedObject.getConcept(),
                detectedObject.getX1(), detectedObject.getY1(), detectedObject.getX2(), detectedObject.getY2(),
                false,
                detectedObject.getProcess(),
                new LumifyVisibility().getVisibility(),
                this.user.getModelUserContext());
    }

    public List<ArtifactDetectedObject> detectObjects(BufferedImage bImage) {
        List<ArtifactDetectedObject> detectedObjectList = new ArrayList<ArtifactDetectedObject>();
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
                            objectClassifier.concept,
                            this.getClass().getName());
                    detectedObjectList.add(detectedObject);
                }
            }
        }
        return detectedObjectList;
    }

    @Override
    public boolean isHandled(Vertex vertex, Property property) {
        String mimeType = (String) property.getMetadata().get(RawLumifyProperties.MIME_TYPE.getKey());
        return !(mimeType == null || !mimeType.startsWith("image"));
    }

    private class CascadeClassifierHolder {
        public final String concept;
        public final CascadeClassifier cascadeClassifier;

        public CascadeClassifierHolder(String concept, CascadeClassifier cascadeClassifier) {
            this.concept = concept;
            this.cascadeClassifier = cascadeClassifier;
        }
    }

    @Inject
    public void setDetectedObjectRepository(DetectedObjectRepository detectedObjectRepository) {
        this.detectedObjectRepository = detectedObjectRepository;
    }
}
