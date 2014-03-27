package com.altamiracorp.lumify.storm.image;

import com.altamiracorp.lumify.core.ingest.AdditionalArtifactWorkData;
import com.altamiracorp.lumify.core.ingest.ArtifactDetectedObject;
import com.altamiracorp.lumify.core.ingest.ArtifactExtractedInfo;
import com.altamiracorp.lumify.core.ingest.TextExtractionWorkerPrepareData;
import com.altamiracorp.lumify.core.util.LumifyLogger;
import com.altamiracorp.lumify.core.util.LumifyLoggerFactory;
import com.altamiracorp.lumify.objectDetection.ObjectDetector;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.ServiceLoader;

public class ImageObjectDetectionWorker extends BaseImageWorker {
    private static final LumifyLogger LOGGER = LumifyLoggerFactory.getLogger(ImageObjectDetectionWorker.class);

    private List<ObjectDetector> objectDetectors;

    @Override
    protected ArtifactExtractedInfo doWork(BufferedImage image, AdditionalArtifactWorkData data) throws Exception {
        LOGGER.debug("Detecting Objects [ImageObjectDetectionWorker]: %s", data.getFileName());
        List<ArtifactDetectedObject> detectedObjects = new ArrayList<ArtifactDetectedObject>();
        ArtifactExtractedInfo info = new ArtifactExtractedInfo();

        for (ObjectDetector objectDetector : objectDetectors) {
            for (ArtifactDetectedObject detectedObject : objectDetector.detectObjects(image)) {
                detectedObjects.add(detectedObject);
            }
        }

        info.setDetectedObjects(detectedObjects);
        LOGGER.debug("Finished [ImageObjectDetectionWorker]: %s", data.getFileName());
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
