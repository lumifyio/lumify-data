package com.altamiracorp.reddawn.contentTypeExtraction;

import com.altamiracorp.reddawn.RedDawnSession;
import com.altamiracorp.reddawn.ucd.artifact.Artifact;
import com.altamiracorp.reddawn.ucd.artifact.ArtifactGenericMetadata;
import com.altamiracorp.reddawn.ucd.artifact.ArtifactRepository;
import com.altamiracorp.reddawn.ucd.artifact.ArtifactRowKey;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.io.Text;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.io.InputStream;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;


@RunWith(JUnit4.class)
public class ContentTypeMRTest {
    ContentTypeMR.ContentTypeMapper mapper;

    @Before
    public void setup () {
        mapper = new ContentTypeMR.ContentTypeMapper ();
    }

    @Test
    public void testContentTypeMapper () throws Exception{
        InputStream in = TikaContentTypeExtractor.class.getResourceAsStream("/H_264.m4v");

        Artifact mockArtifact = mock (Artifact.class);
        ArtifactRowKey mockArtifactRowKey = mock (ArtifactRowKey.class);
        ArtifactRepository mockArtifactRepo = mock (ArtifactRepository.class);
        ArtifactGenericMetadata mockArtifactGenericMetadata = mock (ArtifactGenericMetadata.class);
        RedDawnSession mockSession = mock (RedDawnSession.class);
        ContentTypeExtractor mockContentTypeExtractor = mock (ContentTypeExtractor.class);
        Mapper.Context mockContext = mock (Mapper.Context.class);

        when(mockArtifact.getRowKey()).thenReturn(mockArtifactRowKey);
        when (mockArtifactRowKey.toString()).thenReturn("Temp Output");
        when (mockArtifactRepo.getRaw(mockSession.getModelSession(), mockArtifact)).thenReturn(in);
        when (mockContentTypeExtractor.extract(in)).thenReturn("video/x-m4v");
        when (mockArtifact.getGenericMetadata()).thenReturn(mockArtifactGenericMetadata);
        when (mockArtifactGenericMetadata.setMimeType("video/x-m4v")).thenReturn(mockArtifactGenericMetadata);

        mapper.setArtifactRepository(mockArtifactRepo);
        mapper.setContentTypeExtractor(mockContentTypeExtractor);
        mapper.setSession(mockSession);
        mapper.map (mock(Text.class), mockArtifact, mockContext);
        verify(mockContext, times(1)).write (any(Text.class), eq(mockArtifact));
    }

}
