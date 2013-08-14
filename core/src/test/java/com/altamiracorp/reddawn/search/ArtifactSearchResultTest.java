package com.altamiracorp.reddawn.search;

import com.altamiracorp.reddawn.ucd.artifact.ArtifactType;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(JUnit4.class)
public class ArtifactSearchResultTest {
    ArtifactSearchResult asr;
    String sampleRowKey = "myRowKey";
    String sampleSubject = "mySubject";
    Date sampleDate = mock(Date.class);
    String sampleSource = "mySource";
    String graphNodeId = "myGraphNodeId";

    @Before
    public void setUp() throws Exception {
        asr = new ArtifactSearchResult(sampleRowKey, sampleSubject, sampleDate, sampleSource, ArtifactType.DOCUMENT, graphNodeId);
    }

    @Test
    public void testGetRowKey() throws Exception {
        String result = asr.getRowKey();
        assertEquals(sampleRowKey, result);
    }

    @Test
    public void testGetSubject() throws Exception {
        String result = asr.getSubject();
        assertEquals(sampleSubject, result);
    }

    @Test
    public void testGetPublishedDate() throws Exception {
        when(sampleDate.toString()).thenReturn("sampleDate");
        Date result = asr.getPublishedDate();
        assertTrue(sampleDate.equals(result));
    }

    @Test
    public void testGetSource() throws Exception {
        String result = asr.getSource();
        assertEquals(sampleSource, result);
    }

    @Test
    public void testGetGraphNodeId() throws Exception {
        String result = asr.getGraphVertexId();
        assertEquals(graphNodeId, result);
    }

    @Test
    public void testToString() throws Exception {
        String expectedToString = "rowKey: " + sampleRowKey + ", subject: " + sampleSubject +
                ", publishedDate: " + sampleDate + ", source: " + sampleSource + ", graphVertexId: " + graphNodeId;
        String result = asr.toString();
        assertEquals(expectedToString, result);
    }
}
