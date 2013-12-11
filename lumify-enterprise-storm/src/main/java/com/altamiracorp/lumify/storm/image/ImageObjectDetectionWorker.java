package com.altamiracorp.lumify.storm.image;

import com.altamiracorp.lumify.core.ingest.AdditionalArtifactWorkData;
import com.altamiracorp.lumify.core.ingest.ArtifactDetectedObject;
import com.altamiracorp.lumify.core.ingest.ArtifactExtractedInfo;
import com.altamiracorp.lumify.core.ingest.TextExtractionWorkerPrepareData;
import com.altamiracorp.lumify.core.model.artifact.ArtifactType;
import com.altamiracorp.lumify.objectDetection.ObjectDetector;
import org.json.JSONArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.ServiceLoader;

public class ImageObjectDetectionWorker extends BaseImageWorker {

    private static final Logger LOGGER = LoggerFactory.getLogger(ImageObjectDetectionWorker.class.getName());

    private List<ObjectDetector> objectDetectors;

    @Override
    protected ArtifactExtractedInfo doWork(BufferedImage image, AdditionalArtifactWorkData data) throws Exception {
        LOGGER.debug("Detecting Objects [ImageObjectDetectionWorker]: " + data.getFileName());
        JSONArray detectedObjectsJson = new JSONArray();
        ArtifactExtractedInfo info = new ArtifactExtractedInfo();

        for (ObjectDetector objectDetector : objectDetectors) {
            List<ArtifactDetectedObject> detectedObjects = objectDetector.detectObjects(image);
            for (ArtifactDetectedObject detectedObject : detectedObjects) {
                detectedObject.setConcept(detectedObject.getConcept());
                detectedObjectsJson.put(detectedObject.getJson());
            }
        }

        info.setDetectedObjects(detectedObjectsJson.toString());
        info.setArtifactType(ArtifactType.IMAGE.toString());
        LOGGER.debug("Finished [ImageObjectDetectionWorker]: " + data.getFileName());
        return info;
    }

    @Override
    public void prepare(TextExtractionWorkerPrepareData data) throws Exception {
        objectDetectors = new ArrayList<ObjectDetector>();
        for (ObjectDetector od : getObjectDetectors()) {
            data.getInjector().injectMembers(od);
            od.init(data.getStormConf(), data.getHdfsFileSystem());
            objectDetectors.add(od);
        }
    }

    protected Collection<ObjectDetector> getObjectDetectors() {
        ArrayList<ObjectDetector> objectDetectors = new ArrayList<ObjectDetector>();
        for (ObjectDetector objectDetector : ServiceLoader.load(ObjectDetector.class)) {
            objectDetectors.add(objectDetector);
        }
        return objectDetectors;
    }
}
