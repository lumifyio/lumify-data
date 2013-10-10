package com.altamiracorp.lumify.entityExtraction;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.apache.hadoop.conf.Configuration;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.altamiracorp.lumify.core.ingest.termExtraction.TermExtractionResult;
import com.altamiracorp.lumify.core.ingest.termExtraction.TermExtractionResult.TermMention;
import com.altamiracorp.lumify.core.user.User;
import com.google.common.base.Charsets;

@RunWith(MockitoJUnitRunner.class)
public class RegexEntityExtractorTest extends BaseExtractorTest {

    private static final String EMAIL_REG_EX = "(?i)\\b[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,4}\\b";

    private static final String EMAIL_TYPE = "emailAddress";

    private RegexEntityExtractor extractor;

    @Mock
    private Configuration config;

    @Mock
    private User user;

    private String textWith = "This is some text that contains an e-mail address for Bob, bob@gmail.com and an e-mail address for Bob's friend Bill, bill@outlook.com.";
    private String textWithNewlines = "This is some text that contains\n an e-mail address for Bob, bob@gmail.com and an e-mail address\n for Bob's friend Bill, bill@outlook.com.";
    private String textWithout = "This is some text that contains no e-mail address";

    @Before
    public void setUp() throws IOException {
        when(config.get(RegexEntityExtractor.REGULAR_EXPRESSION)).thenReturn(EMAIL_REG_EX);
        when(config.get(RegexEntityExtractor.ENTITY_TYPE)).thenReturn(EMAIL_TYPE);

        extractor = new RegexEntityExtractor();
        extractor.prepare(config,user);
    }

    @Test
    public void testRegularExpressionExtraction() throws Exception {
        final TermExtractionResult result = extractor.extract(asStream(textWith));
        assertNotNull(result);

        final List<TermMention> termMentions = result.getTermMentions();
        assertEquals("Incorrect number of email addresses extracted", 2, termMentions.size());

        TermMention firstTerm = termMentions.get(0);
        assertEquals(EMAIL_TYPE, firstTerm.getOntologyClassUri());
        assertEquals("bob@gmail.com", firstTerm.getSign());
        assertEquals(59, firstTerm.getStart());
        assertEquals(72, firstTerm.getEnd());

        TermMention secondTerm = termMentions.get(1);
        assertEquals(EMAIL_TYPE, firstTerm.getOntologyClassUri());
        assertEquals("bill@outlook.com", secondTerm.getSign());
        assertEquals(118, secondTerm.getStart());
        assertEquals(134, secondTerm.getEnd());
    }

    @Test
    public void testRegularExpressionExtractionWithNewlines() throws Exception {
        final TermExtractionResult result = extractor.extract(asStream(textWithNewlines));
        assertNotNull(result);

        final List<TermMention> termMentions = result.getTermMentions();
        assertEquals("Incorrect number of email addresses extracted", 2, termMentions.size());

        TermMention firstTerm = termMentions.get(0);
        assertEquals(EMAIL_TYPE, firstTerm.getOntologyClassUri());
        assertEquals("bob@gmail.com", firstTerm.getSign());
        assertEquals(60, firstTerm.getStart());
        assertEquals(73, firstTerm.getEnd());

        TermMention secondTerm = termMentions.get(1);
        assertEquals(EMAIL_TYPE, firstTerm.getOntologyClassUri());
        assertEquals("bill@outlook.com", secondTerm.getSign());
        assertEquals(120, secondTerm.getStart());
        assertEquals(136, secondTerm.getEnd());
    }

    @Test
    public void testNegativeRegularExpressionExtraction() throws Exception {
        final TermExtractionResult result = extractor.extract(asStream(textWithout));
        assertNotNull(result);
        assertTrue(result.getTermMentions().isEmpty());
    }

    private InputStream asStream(final String text) {
        return new ByteArrayInputStream(text.getBytes(Charsets.UTF_8));
    }
}

