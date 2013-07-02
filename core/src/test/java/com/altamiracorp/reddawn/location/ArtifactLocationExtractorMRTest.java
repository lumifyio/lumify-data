package com.altamiracorp.reddawn.location;

import com.altamiracorp.reddawn.ucd.artifact.Artifact;
import com.altamiracorp.reddawn.ucd.artifact.ArtifactRowKey;
import com.altamiracorp.reddawn.ucd.term.Term;
import com.altamiracorp.reddawn.ucd.term.TermRowKey;
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
public class ArtifactLocationExtractorMRTest {
    ArtifactLocationExtractorMR.ArtifactLocationExtractorMapper mapper;

    @Before
    public void setUp () throws Exception{
        mapper = new ArtifactLocationExtractorMR.ArtifactLocationExtractorMapper();
    }

    @Test
    public void testArtifactExtractorMapperMap () throws Exception{
        ArtifactLocationExtractor mockExtractor = mock (ArtifactLocationExtractor.class);

        Term mockTerm = mock(Term.class);
        TermRowKey mockTermRowKey = mock(TermRowKey.class);
        when(mockTerm.getRowKey()).thenReturn(mockTermRowKey);
        when(mockTermRowKey.toString()).thenReturn("Temp Output");

        Artifact mockArtifact1 = mock(Artifact.class);
        Artifact mockArtifact2 = mock(Artifact.class);

        when(mockExtractor.extract(mockTerm)).thenReturn(
                new ArrayList<Artifact>(Arrays.asList(new Artifact[]{mockArtifact1,mockArtifact2})));
        Whitebox.setInternalState(mapper, ArtifactLocationExtractor.class, mockExtractor);

        Mapper.Context mockContext = mock(Mapper.Context.class);
        mapper.map(mock(Text.class), mockTerm, mockContext);
        verify(mockContext, times(2)).write(any(Text.class), any(Artifact.class));
    }
}
