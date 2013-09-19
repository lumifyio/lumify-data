package com.altamiracorp.lumify.entityHighlight;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.altamiracorp.lumify.core.user.User;
import com.altamiracorp.lumify.ucd.artifact.Artifact;
import com.altamiracorp.lumify.ucd.artifact.ArtifactContent;
import com.altamiracorp.lumify.ucd.artifact.ArtifactRepository;

public class EntityHighlightWorkerTest {

    private static final String HIGHLIGHTED_TEXT = "highlighted!";
    private static final String ARTIFACT_KEY = "FOO";

    @Mock
    private Artifact artifactMock;

    @Mock
    private ArtifactContent contentMock;

    @Mock
    ArtifactRepository artifactRepository;

    @Mock
    EntityHighlighter highlighter;

    @Mock
    private User user;

    private EntityHighlightWorker worker;

    @Before
    public void setupTests() {
        MockitoAnnotations.initMocks(this);

        worker = new EntityHighlightWorker(artifactRepository, highlighter, ARTIFACT_KEY, user);
    }

    @Test(expected = NullPointerException.class)
    public void testHighlightWorkerCreationInvalidArtifactRepo() {
        new EntityHighlightWorker(null, highlighter, ARTIFACT_KEY, user);
    }

    @Test(expected = NullPointerException.class)
    public void testHighlightWorkerCreationInvalidHighlighter() {
        new EntityHighlightWorker(artifactRepository, null, ARTIFACT_KEY, user);
    }

    @Test(expected = NullPointerException.class)
    public void testHighlightWorkerCreationInvalidArtifactKey() {
        new EntityHighlightWorker(artifactRepository, highlighter, null, user);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testHighlightWorkerCreationEmptyArtifactKey() {
        new EntityHighlightWorker(artifactRepository, highlighter, "", user);
    }

    @Test
    public void testHighlightInvalidArtifact() {
        // Configure the repo to return an invalid artifact
        when(artifactRepository.findByRowKey(anyString(), eq(user))).thenReturn(null);

        worker.run();
        verify(artifactRepository, times(1)).findByRowKey(anyString(), eq(user));
    }

    @Test
    public void testArtifactInvalidHighlightText() {
        // Configure the repo to return an invalid artifact
        when(artifactRepository.findByRowKey(anyString(), eq(user))).thenReturn(artifactMock);
        when(highlighter.getHighlightedText(any(Artifact.class), eq(user))).thenReturn(null);

        worker.run();
        verify(artifactRepository, times(1)).findByRowKey(anyString(), eq(user));
    }


    @Test
    public void testArtifactEmptyHighlightText() {
        // Configure the repo to return an invalid artifact
        when(artifactRepository.findByRowKey(anyString(), eq(user))).thenReturn(artifactMock);
        when(highlighter.getHighlightedText(any(Artifact.class), eq(user))).thenReturn("");

        worker.run();
        verify(artifactRepository, times(1)).findByRowKey(anyString(), eq(user));
    }

    @Test
    public void testArtifactHighlightText() {
        // Configure the repo to return an invalid artifact
        when(artifactRepository.findByRowKey(anyString(), eq(user))).thenReturn(artifactMock);
        when(artifactMock.getContent()).thenReturn(contentMock);
        when(highlighter.getHighlightedText(any(Artifact.class), eq(user))).thenReturn(HIGHLIGHTED_TEXT);

        worker.run();
        verify(artifactRepository, times(1)).findByRowKey(anyString(), eq(user));
        verify(contentMock, times(1)).setHighlightedText(HIGHLIGHTED_TEXT);
        verify(artifactRepository, times(1)).save(any(Artifact.class), eq(user));
    }
}
