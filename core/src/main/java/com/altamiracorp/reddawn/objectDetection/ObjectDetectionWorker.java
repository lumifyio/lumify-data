package com.altamiracorp.reddawn.objectDetection;

import com.altamiracorp.reddawn.RedDawnSession;
import com.altamiracorp.reddawn.ucd.artifact.Artifact;
import com.altamiracorp.reddawn.ucd.artifact.ArtifactDetectedObjects;
import com.altamiracorp.reddawn.ucd.artifact.ArtifactRepository;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Responsible for modifying the html tag of an {@link com.altamiracorp.reddawn.ucd.artifact.ArtifactDetectedObjects}
 * in a background thread.
 */

public class ObjectDetectionWorker implements Runnable {
    private static final Logger LOGGER = LoggerFactory.getLogger(ObjectDetectionWorker.class.getName());
    private final ArtifactRepository artifactRepository = new ArtifactRepository();
    private final RedDawnSession session;
    private final String artifactKey;
    private final String columnName;
    private final List<String> cssClass;
    private final JSONObject info;

    public ObjectDetectionWorker (final RedDawnSession session, final String artifactKey, final String columnName,
                                  final List<String> cssClass, final JSONObject info) {
        checkNotNull(session);
        checkNotNull(artifactKey);
        checkArgument(!artifactKey.isEmpty(), "The provided artifact key is empty");
        checkNotNull(columnName);
        checkArgument(!columnName.isEmpty(), "The provided column name is empty");
        checkNotNull(cssClass);
        checkArgument(!(cssClass.size() < 1), "The provided css classes is empty");

        this.session = session;
        this.artifactKey = artifactKey;
        this.columnName = columnName;
        this.cssClass = cssClass;
        this.info = info;
    }

    @Override
    public void run () {
        LOGGER.info("Modifying object detection html tag for artifact with key: " + artifactKey);

        final long startTime = System.currentTimeMillis();
        final Artifact artifact = artifactRepository.findByRowKey(session.getModelSession(), artifactKey);

        if (artifact != null){
            if ( modifyObjectDetection(session, artifact)){
                LOGGER.info(String.format("Resolving object detection for artifact took: %d ms", System.currentTimeMillis() - startTime));
            }
        } else {
            LOGGER.warn("Could not find artifact with key: " + artifactKey);
        }
    }

    private boolean modifyObjectDetection (final RedDawnSession session, final Artifact artifact){
        boolean modified = false;
        ArtifactDetectedObjects artifactDetectedObjects = artifact.getArtifactDetectedObjects();
        final String detectedObjects = artifactDetectedObjects.getDetectedObjectValue (cssClass, info);

        if (detectedObjects != null && !detectedObjects.isEmpty()) {
            artifact.getArtifactDetectedObjects().set(columnName, detectedObjects);
            artifactRepository.save(session.getModelSession(), artifact);
            modified = true;
        } else {
            LOGGER.info("Could not retrieve valid column value for detected object");
        }
        return modified;
    }
}
