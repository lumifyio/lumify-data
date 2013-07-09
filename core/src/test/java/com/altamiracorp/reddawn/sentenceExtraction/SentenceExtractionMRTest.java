package com.altamiracorp.reddawn.sentenceExtraction;

import com.altamiracorp.reddawn.ucd.artifact.Artifact;
import com.altamiracorp.reddawn.ucd.artifact.ArtifactRowKey;
import com.altamiracorp.reddawn.ucd.artifact.ArtifactType;
import com.altamiracorp.reddawn.ucd.sentence.Sentence;
import edu.emory.mathcs.backport.java.util.Arrays;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.powermock.reflect.Whitebox;

import java.util.ArrayList;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

@RunWith(JUnit4.class)
public class SentenceExtractionMRTest {

    SentenceExtractionMR.SentenceExtractorMapper mapper;

    @Before
    public void setUp() throws Exception {
        mapper = new SentenceExtractionMR.SentenceExtractorMapper();
    }

    @Test
    public void testSentenceExtractorMapperMap() throws Exception {
        SentenceExtractor mockExtractor = mock(SentenceExtractor.class);

        Artifact mockArtifact = mock(Artifact.class);
        ArtifactRowKey mockArtifactRK = mock(ArtifactRowKey.class);
        when(mockArtifact.getRowKey()).thenReturn(mockArtifactRK);
        when(mockArtifact.getType()).thenReturn(ArtifactType.DOCUMENT);
        when(mockArtifactRK.toString()).thenReturn("I don't care about this output");

        Sentence mockSentence1 = mock(Sentence.class);
        Sentence mockSentence2 = mock(Sentence.class);
        when(mockExtractor.extractSentences(mockArtifact)).thenReturn(
                new ArrayList<Sentence>(Arrays.asList(new Sentence[]{mockSentence1, mockSentence2})));
        Whitebox.setInternalState(mapper, SentenceExtractor.class, mockExtractor);

        Mapper.Context mockContext = mock(Mapper.Context.class);
        mapper.map(mock(Text.class), mockArtifact, mockContext);
        verify(mockContext, times(2)).write(any(Text.class), any(Sentence.class));
    }
}
