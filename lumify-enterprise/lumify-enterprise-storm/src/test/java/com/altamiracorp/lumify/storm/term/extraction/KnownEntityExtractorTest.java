package com.altamiracorp.lumify.storm.term.extraction;


import com.altamiracorp.lumify.core.ingest.term.extraction.TermExtractionResult;
import com.altamiracorp.lumify.core.user.User;
import org.apache.hadoop.conf.Configuration;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.InputStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.altamiracorp.lumify.core.ingest.term.extraction.TermMention;

@RunWith(MockitoJUnitRunner.class)
public class KnownEntityExtractorTest {
    private KnownEntityExtractor extractor;
    @Mock
    private User user;
    private Configuration config;
    String dictionaryPath;


    @Before
    public void setup() {
        dictionaryPath = getClass().getResource(".").getPath();
        extractor = new KnownEntityExtractor();
        config = new Configuration();
        config.set("termextraction.knownEntities.pathPrefix", "file://" + dictionaryPath);
    }

    @Test
    public void textExtract() throws Exception {

        extractor.prepare(config, user);
        InputStream in = getClass().getResourceAsStream("bffls.txt");
        TermExtractionResult result = extractor.extract(in);
        assertEquals(3, result.getTermMentions().size());
        for (TermMention termMention : result.getTermMentions()) {
            assertTrue(termMention.isResolved());
            assertEquals("person", termMention.getOntologyClassUri());
            assertEquals("Joe Ferner", termMention.getSign());
        }


    }


}
