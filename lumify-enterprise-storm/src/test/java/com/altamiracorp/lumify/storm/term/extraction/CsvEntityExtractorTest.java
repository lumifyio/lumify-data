package com.altamiracorp.lumify.storm.term.extraction;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.text.ParseException;

import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.altamiracorp.bigtable.model.user.ModelUserContext;
import com.altamiracorp.lumify.core.ingest.term.extraction.TermExtractionResult;
import com.altamiracorp.lumify.core.ingest.term.extraction.TermMention;
import com.altamiracorp.lumify.core.ingest.term.extraction.TermRelationship;
import com.altamiracorp.lumify.core.model.artifact.Artifact;
import com.altamiracorp.lumify.core.model.artifact.ArtifactMetadata;
import com.altamiracorp.lumify.core.model.artifact.ArtifactRepository;
import com.altamiracorp.lumify.core.model.graph.GraphVertex;
import com.altamiracorp.lumify.core.user.User;

@RunWith(MockitoJUnitRunner.class)
public class CsvEntityExtractorTest {
    private CsvEntityExtractor extractor;
    @Mock
    private GraphVertex vertex;
    @Mock
    private Artifact artifact;
    @Mock
    private ArtifactMetadata metadata;
    @Mock
    private User user;
    @Mock
    private ArtifactRepository artifactRepository;

    @Test
    public void testExtract() throws IOException, ParseException {
        extractor = new CsvEntityExtractor();
        extractor.setArtifactRepository(artifactRepository);
        when(artifactRepository
                .findByRowKey(anyString(), any(ModelUserContext.class)))
                .thenReturn(artifact);

        when(artifact.getMetadata())
                .thenReturn(metadata);
        when(metadata.getText())
                .thenReturn(IOUtils.toString(getClass().getResourceAsStream("personLocations.csv")));
        when(metadata.getMappingJson())
                .thenReturn(IOUtils.toString(getClass().getResourceAsStream("personLocations.csv.mapping.json")));


        TermExtractionResult result = extractor.extract(vertex, user);
        assertNotNull(result);

        TermMention mention1 = result.getTermMentions().remove(0);
        TermMention mention2 = result.getTermMentions().remove(0);

        assertTrue(mention1.isResolved());
        assertEquals(14, mention1.getStart());
        assertEquals(24, mention1.getEnd());
        assertEquals("person", mention1.getOntologyClassUri());
        assertEquals("Joe Ferner", mention1.getSign());
        assertTrue(mention1.getPropertyValue().containsKey("birthDate"));

        assertTrue(mention2.isResolved());
        assertEquals(25, mention2.getStart());
        assertEquals(30, mention2.getEnd());
        assertEquals("location", mention2.getOntologyClassUri());
        assertEquals("20147", mention2.getSign());

        assertEquals(result.getRelationships().size(), 1);
        TermRelationship relationship = result.getRelationships().get(0);
        assertEquals(relationship.getSourceTermMention(), mention1);
        assertEquals(relationship.getDestTermMention(), mention2);
        assertEquals("personLivesAtLocation", relationship.getLabel());

        verify(artifactRepository, times(1)).findByRowKey(anyString(), any(ModelUserContext.class));
        verify(artifact, times(3)).getMetadata();
        verify(metadata, times(1)).getText();
        verify(metadata, times(2)).getMappingJson();

    }

}
