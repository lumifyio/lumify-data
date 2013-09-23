package com.altamiracorp.lumify.entityHighlight;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.altamiracorp.lumify.core.user.User;
import com.altamiracorp.lumify.ucd.artifact.Artifact;
import com.altamiracorp.lumify.ucd.artifact.ArtifactRepository;

/**
 * Responsible for modifying the highlighted text portion of an {@link Artifact}
 * in a background thread.
 */
public final class EntityHighlightWorker implements Runnable {
    private static final Logger LOGGER = LoggerFactory.getLogger(EntityHighlightWorker.class.getName());

    private final User user;
    private final ArtifactRepository artifactRepository;
    private final EntityHighlighter highlighter;
    private final String artifactKey;

    public EntityHighlightWorker(ArtifactRepository artifactRepository, EntityHighlighter highlighter, final String artifactKey, User user) {
        checkNotNull(artifactRepository);
        checkNotNull(highlighter);
        checkNotNull(artifactKey);
        checkArgument(!artifactKey.isEmpty(), "The provided artifact key is empty");
        checkNotNull(user);

        this.artifactRepository = artifactRepository;
        this.highlighter = highlighter;
        this.user = user;
        this.artifactKey = artifactKey;
    }

    @Override
    public void run() {
        LOGGER.info("Modifying highlighted text for artifact with key: " + artifactKey);

        final long startTime = System.currentTimeMillis();
        final Artifact artifact = artifactRepository.findByRowKey(artifactKey, user);

        if (artifact != null) {
            if (modifyHighlightedText(artifact, user)) {
                LOGGER.info(String.format("Text highlighting for artifact took: %d ms", System.currentTimeMillis() - startTime));
            }
        } else {
            LOGGER.warn("Could not find artifact with key: " + artifactKey);
        }
    }


    private boolean modifyHighlightedText(final Artifact artifact, User user) {
        boolean modified = false;
        final String highlightedText = highlighter.getHighlightedText(artifact, user);

        if (highlightedText != null && !highlightedText.isEmpty()) {
            artifact.getContent().setHighlightedText(highlightedText);

            artifactRepository.save(artifact, user);
            modified = true;
        } else {
            LOGGER.info("Could not retrieve valid highlighted text for artifact");
        }

        return modified;
    }
}
