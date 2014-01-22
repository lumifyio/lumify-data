package com.altamiracorp.lumify.storm.term.extraction;

import com.altamiracorp.lumify.core.ingest.term.extraction.TermExtractionResult;
import com.altamiracorp.lumify.core.ingest.term.extraction.TermMention;
import com.altamiracorp.lumify.core.ingest.term.extraction.TermRelationship;
import com.altamiracorp.lumify.core.user.User;
import com.altamiracorp.securegraph.Vertex;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.IOException;
import java.text.ParseException;

import static org.junit.Assert.*;

@RunWith(MockitoJUnitRunner.class)
public class CsvEntityExtractorTest {
    private CsvEntityExtractor extractor;
    @Mock
    private Vertex vertex;
    @Mock
    private User user;

    @Test
    public void testExtract() throws IOException, ParseException {
        extractor = new CsvEntityExtractor();

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
    }

}
