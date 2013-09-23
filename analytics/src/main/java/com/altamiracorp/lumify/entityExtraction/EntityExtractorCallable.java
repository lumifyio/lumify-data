package com.altamiracorp.lumify.entityExtraction;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.List;
import java.util.concurrent.Callable;

import org.apache.hadoop.thirdparty.guava.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.altamiracorp.lumify.ucd.artifact.Artifact;

/**
 * Allows for the extraction execution of an {@link EntityExtractor} to be performed on a separate thread.
 */
public class EntityExtractorCallable implements Callable<List<ExtractedEntity>> {
    private static final Logger LOGGER = LoggerFactory.getLogger(EntityExtractorCallable.class);

    private final EntityExtractor extractor;
    private final Artifact artifact;
    private final String artifactText;

    public EntityExtractorCallable(final EntityExtractor extractor, final Artifact artifact,
            final String artifactText) {
        checkNotNull(extractor);
        checkNotNull(artifact);
        checkNotNull(artifactText);

        this.extractor = extractor;
        this.artifact = artifact;
        this.artifactText = artifactText;
    }

    @Override
    public List<ExtractedEntity> call() throws Exception {
        List<ExtractedEntity> entities = Lists.newArrayList();

        try {
            entities = extractor.extract(artifact, artifactText);
            LOGGER.debug("Entity extraction finished");
        } catch (Exception e) {
            LOGGER.error("Error occurred while extracting entities from artifact: " + artifact.getRowKey(), e);
            throw e;
        }

        return entities;
    }
}
