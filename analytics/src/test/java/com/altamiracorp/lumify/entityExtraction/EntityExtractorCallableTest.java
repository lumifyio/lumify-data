package com.altamiracorp.lumify.entityExtraction;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.concurrent.Callable;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.altamiracorp.lumify.ucd.artifact.Artifact;
import com.google.common.collect.ImmutableList;

public class EntityExtractorCallableTest {

    private static final String ARTIFACT_TEXT = "The quick brown fox.";

    private Callable<List<ExtractedEntity>> callable;

    @Mock
    private Artifact artifact;

    @Mock
    private EntityExtractor extractor;

    @Before
    public void setupTests() {
        MockitoAnnotations.initMocks(this);

        callable = new EntityExtractorCallable(extractor, artifact, ARTIFACT_TEXT);
    }

    @Test(expected = NullPointerException.class)
    public void testExtractorCallableInvalidExtractor() {
        new EntityExtractorCallable(null, artifact, ARTIFACT_TEXT);
    }

    @Test(expected = NullPointerException.class)
    public void testExtractorCallableInvalidArtifact() {
        new EntityExtractorCallable(extractor, null, ARTIFACT_TEXT);
    }

    @Test(expected = NullPointerException.class)
    public void testExtractorCallableInvalidArtifactText() {
        new EntityExtractorCallable(extractor, artifact, null);
    }

    @Test
    public void testExtractorCallableCallWithEntities() throws Exception {
        when(extractor.extract(eq(artifact), anyString()))
            .thenReturn(ImmutableList.<ExtractedEntity>of(new ExtractedEntity(null, null)));

        final List<ExtractedEntity> results = callable.call();
        assertEquals(1, results.size());
        assertNull(results.get(0).getGraphVertex());
        assertNull(results.get(0).getTermMention());

        verify(extractor, times(1)).extract(eq(artifact), anyString());
    }

    @Test(expected = Exception.class)
    public void testExtractorCallableCallWithException() throws Exception {
        when(extractor.extract(eq(artifact), anyString())).thenThrow(new Exception());

        callable.call();
    }
}
