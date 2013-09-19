package com.altamiracorp.lumify.entityHighlight;

import com.altamiracorp.lumify.AppSession;
import com.altamiracorp.lumify.core.user.User;
import com.altamiracorp.lumify.ucd.artifact.Artifact;
import com.altamiracorp.lumify.ucd.artifact.ArtifactContent;
import com.altamiracorp.lumify.ucd.artifact.ArtifactRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.internal.util.reflection.Whitebox;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

@RunWith(PowerMockRunner.class)
public class EntityHighlightWorkerTest {

    private static final String HIGHLIGHTER_FIELD = "highlighter";
    private static final String ARTIFACT_REPO_FIELD = "artifactRepository";
    private static final String HIGHLIGHTED_TEXT = "highlighted!";
    private static final String ARTIFACT_KEY = "FOO";

    private AppSession sessionMock;
    private ArtifactRepository artifactRepoMock;
    private EntityHighlighter highlighterMock;
    private Artifact artifactMock;
    private ArtifactContent contentMock;

    private EntityHighlightWorker worker;

    @Mock
    ArtifactRepository artifactRepository;

    @Mock
    EntityHighlighter highlighter;

    @Mock
    private User user;

    @Before
    public void setupTests() {
        artifactRepoMock = mock(ArtifactRepository.class);
        highlighterMock = mock(EntityHighlighter.class);
        artifactMock = mock(Artifact.class);
        contentMock = mock(ArtifactContent.class);

        worker = new EntityHighlightWorker(artifactRepository, highlighter, ARTIFACT_KEY, user);
    }

    @Test(expected = NullPointerException.class)
    public void testHighlightWorkerCreationInvalidSession() {
        new EntityHighlightWorker(artifactRepository, highlighter, ARTIFACT_KEY, user);
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
        when(artifactRepoMock.findByRowKey(anyString(), user)).thenReturn(null);

        Whitebox.setInternalState(worker, ARTIFACT_REPO_FIELD, artifactRepoMock);

        worker.run();
        verify(sessionMock, times(1)).getModelSession();
        verify(artifactRepoMock, times(1)).findByRowKey(anyString(), user);
    }

    @Test
    public void testArtifactInvalidHighlightText() {
        // Configure the repo to return an invalid artifact
        when(artifactRepoMock.findByRowKey(anyString(), user)).thenReturn(artifactMock);

        when(highlighterMock.getHighlightedText(any(Artifact.class), user)).thenReturn(null);

        Whitebox.setInternalState(worker, ARTIFACT_REPO_FIELD, artifactRepoMock);
        Whitebox.setInternalState(worker, HIGHLIGHTER_FIELD, highlighterMock);

        worker.run();
        verify(sessionMock, times(1)).getModelSession();
        verify(artifactRepoMock, times(1)).findByRowKey(anyString(), user);
    }


    @Test
    public void testArtifactEmptyHighlightText() {
        // Configure the repo to return an invalid artifact
        when(artifactRepoMock.findByRowKey(anyString(), user)).thenReturn(artifactMock);

        when(highlighterMock.getHighlightedText(any(Artifact.class), user)).thenReturn("");

        Whitebox.setInternalState(worker, ARTIFACT_REPO_FIELD, artifactRepoMock);
        Whitebox.setInternalState(worker, HIGHLIGHTER_FIELD, highlighterMock);

        worker.run();
        verify(sessionMock, times(1)).getModelSession();
        verify(artifactRepoMock, times(1)).findByRowKey(anyString(), user);
    }

    @Test
    public void testArtifactHighlightText() {
        // Configure the repo to return an invalid artifact
        when(artifactRepoMock.findByRowKey(anyString(), user)).thenReturn(artifactMock);
        when(artifactMock.getContent()).thenReturn(contentMock);
        when(highlighterMock.getHighlightedText(any(Artifact.class), user)).thenReturn(HIGHLIGHTED_TEXT);

        Whitebox.setInternalState(worker, ARTIFACT_REPO_FIELD, artifactRepoMock);
        Whitebox.setInternalState(worker, HIGHLIGHTER_FIELD, highlighterMock);

        worker.run();
        verify(sessionMock, times(2)).getModelSession();
        verify(artifactRepoMock, times(1)).findByRowKey(anyString(), user);
        verify(contentMock, times(1)).setHighlightedText(HIGHLIGHTED_TEXT);
        verify(artifactRepoMock, times(1)).save(any(Artifact.class), user);
    }
}
