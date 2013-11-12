package com.altamiracorp.lumify.storm.image;

import com.altamiracorp.lumify.core.ingest.AdditionalArtifactWorkData;
import com.altamiracorp.lumify.core.ingest.ArtifactDetectedObject;
import com.altamiracorp.lumify.core.ingest.ArtifactExtractedInfo;
import com.altamiracorp.lumify.core.model.artifact.ArtifactType;
import com.altamiracorp.lumify.core.user.User;
import com.altamiracorp.lumify.objectDetection.OpenCVObjectDetector;
import com.google.inject.Inject;
import org.apache.commons.io.IOUtils;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.json.JSONArray;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;

public class ImageObjectDetectionWorker extends BaseImageWorker {

    private static final String OPENCV_CLASSIFIER_CONCEPT_LIST = "objectdetection.classifierConcepts";
    private static final String OPENCV_CLASSIFIER_PATH_PREFIX = "objectdetection.classifier.";
    private static final String OPENCV_CLASSIFIER_PATH_SUFFIX = ".path";

    private OpenCVObjectDetector objectDetector;
    private Map<String, String> classifierFilePaths;

    @Override
    protected ArtifactExtractedInfo doWork(BufferedImage image, AdditionalArtifactWorkData data) throws Exception {
        JSONArray detectedObjectsJson = new JSONArray();
        ArtifactExtractedInfo info = new ArtifactExtractedInfo();

        for (Map.Entry<String, String> classifierFilePath : classifierFilePaths.entrySet()) {
            File localFile = createLocalFile(classifierFilePath.getValue(), data);
            objectDetector.setup(localFile.getPath());
            List<ArtifactDetectedObject> detectedObjects = objectDetector.detectObjects(image);
            for (ArtifactDetectedObject detectedObject : detectedObjects) {
                detectedObject.setConcept(classifierFilePath.getKey());
                detectedObjectsJson.put(detectedObject.getJson());
            }
            localFile.delete();
        }
        info.setDetectedObjects(detectedObjectsJson.toString());
        info.setArtifactType(ArtifactType.IMAGE.toString());
        return info;
    }

    private File createLocalFile(String classifierFilePath, AdditionalArtifactWorkData data) throws IOException {
        File tempFile = File.createTempFile("lumify", ".xml");
        FileOutputStream fos = null;
        InputStream in = null;
        try {
            FileSystem fs = data.getHdfsFileSystem();
            in = fs.open(new Path(classifierFilePath));
            fos = new FileOutputStream(tempFile);
            IOUtils.copy(in, fos);
        } catch (IOException e) {
            e.printStackTrace();
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
    public void prepare(Map stormConf, User user) {
        classifierFilePaths = new HashMap<String, String>();
        String conceptListString = (String) stormConf.get(OPENCV_CLASSIFIER_CONCEPT_LIST);
        checkNotNull(conceptListString, OPENCV_CLASSIFIER_CONCEPT_LIST + " is a required configuration parameter");
        String[] classifierConcepts = conceptListString.split(",");
        for (String classifierConcept : classifierConcepts) {
            classifierFilePaths.put(classifierConcept, (String) stormConf.get(OPENCV_CLASSIFIER_PATH_PREFIX + classifierConcept + OPENCV_CLASSIFIER_PATH_SUFFIX));
        }
    }

    @Inject
    public void setObjectDetector(OpenCVObjectDetector objectDetector) {
        this.objectDetector = objectDetector;
    }
}
