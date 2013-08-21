package com.altamiracorp.reddawn.entityHighlight;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.altamiracorp.reddawn.RedDawnSession;
import com.altamiracorp.reddawn.ucd.artifact.Artifact;
import com.altamiracorp.reddawn.ucd.artifact.ArtifactRepository;

/**
 * Responsible for modifying the highlighted text portion of an {@link Artifact}
 * in a background thread.
 */
public final class EntityHighlightWorker implements Runnable {
    private static final Logger LOGGER = LoggerFactory.getLogger(EntityHighlightWorker.class.getName());

    private final RedDawnSession session;
    private final ArtifactRepository artifactRepository = new ArtifactRepository();
    private final EntityHighlighter highlighter = new EntityHighlighter();
    private final String artifactKey;

    public EntityHighlightWorker(final RedDawnSession session, final String artifactKey) {
        checkNotNull(session);
        checkNotNull(artifactKey);
        checkArgument(!artifactKey.isEmpty(), "The provided artifact key is empty");

        this.session = session;
        this.artifactKey = artifactKey;
    }

    @Override
    public void run() {
        LOGGER.info("Modifying highlighted text for artifact with key: " + artifactKey);

        final long startTime = System.currentTimeMillis();
        final Artifact artifact = artifactRepository.findByRowKey(session.getModelSession(), artifactKey);

        if( artifact != null ) {
            if( modifyHighlightedText(session, artifact) ) {
                LOGGER.info(String.format("Text highlighting for artifact took: %d ms", System.currentTimeMillis() - startTime));
            }
        } else {
            LOGGER.warn("Could not find artifact with key: " + artifactKey);
        }
    }


    private boolean modifyHighlightedText(final RedDawnSession session, final Artifact artifact) {
        boolean modified = false;
        final String highlightedText = highlighter.getHighlightedText(session, artifact);

        if( highlightedText != null && !highlightedText.isEmpty() ) {
                artifact.getContent().setHighlightedText(highlightedText);

                artifactRepository.save(session.getModelSession(), artifact);
                modified = true;
        } else {
            LOGGER.info("Could not retrieve valid highlighted text for artifact");
        }

        return modified;
    }
}
