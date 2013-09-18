package com.altamiracorp.lumify.objectDetection;

import com.altamiracorp.lumify.AppSession;
import com.altamiracorp.lumify.ucd.artifact.Artifact;
import com.altamiracorp.lumify.ucd.artifact.ArtifactRepository;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Responsible for modifying the html tag of an {@link com.altamiracorp.lumify.ucd.artifact.ArtifactDetectedObjects}
 * in a background thread.
 */

public class ObjectDetectionWorker implements Runnable {
    private static final Logger LOGGER = LoggerFactory.getLogger(ObjectDetectionWorker.class.getName());
    private final ArtifactRepository artifactRepository = new ArtifactRepository();
    private final AppSession session;
    private final String artifactKey;
    private final String columnName;
    private final JSONObject info;

    public ObjectDetectionWorker (final AppSession session, final String artifactKey, final String columnName,
                                  final JSONObject info) {
        checkNotNull(session);
        checkNotNull(artifactKey);
        checkArgument(!artifactKey.isEmpty(), "The provided artifact key is empty");
        checkNotNull(columnName);
        checkArgument(!columnName.isEmpty(), "The provided column name is empty");
        checkNotNull(info);

        this.session = session;
        this.artifactKey = artifactKey;
        this.columnName = columnName;
        this.info = info;
    }

    @Override
    public void run () {
        LOGGER.info("Modifying object detection html tag for artifact with key: " + artifactKey);

        final long startTime = System.currentTimeMillis();
        final Artifact artifact = artifactRepository.findByRowKey(session.getModelSession(), artifactKey);

        if (artifact != null){
            try {
                if ( modifyObjectDetection(session, artifact)){
                    LOGGER.info(String.format("Resolving object detection for artifact took: %d ms", System.currentTimeMillis() - startTime));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            LOGGER.warn("Could not find artifact with key: " + artifactKey);
        }
    }

    private boolean modifyObjectDetection (final AppSession session, final Artifact artifact) throws Exception {
        boolean modified = false;

        if (info != null) {
            artifact.getArtifactDetectedObjects().set(columnName, info);
            artifactRepository.save(session.getModelSession(), artifact);
            session.getSearchProvider().add(artifact);
            modified = true;
        } else {
            LOGGER.info("Could not retrieve valid column value for detected object");
        }
        return modified;
    }
}
