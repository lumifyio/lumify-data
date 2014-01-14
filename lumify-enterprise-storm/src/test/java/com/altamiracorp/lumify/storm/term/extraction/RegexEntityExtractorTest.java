package com.altamiracorp.lumify.storm.term.extraction;

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

import com.altamiracorp.lumify.core.ingest.term.extraction.TermExtractionResult;
import com.altamiracorp.lumify.core.ingest.term.extraction.TermMention;
import com.altamiracorp.lumify.core.user.User;
import com.google.common.base.Charsets;

@RunWith(MockitoJUnitRunner.class)
public class RegexEntityExtractorTest {

    private static final String EMAIL_REG_EX = "(?i)\\b[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,4}\\b";
    private static final String EMAIL_TYPE = "emailAddress";

    private static final String EMAIL_TEXT = "This is some text that contains an e-mail address for Bob, bob@gmail.com and an e-mail address for Bob's friend Bill, bill@outlook.com.";
    private static final String EMAIL_NEW_LINES = "This is some text that contains\n an e-mail address for Bob, bob@gmail.com and an e-mail address\n for Bob's friend Bill, bill@outlook.com.";
    private static final String EMAIL_MISSING = "This is some text that contains no e-mail address";

    private static final String ZIPCODE_REG_EX = "\\b\\d{5}-\\d{4}\\b|\\b\\d{5}\\b";
    private static final String LOCATION_TYPE = "location";

    private static final String ZIPCODE_TEXT = "Mr. John Smith 3256, Epiphenomenal Avenue Minneapolis, MN, 55416 or box number PO Box 727050, Defreestville NY 12144-7050";
    private static final String ZIPCODE_NEW_LINES = "Mr. John Smith 3256,\n Epiphenomenal Avenue \nMinneapolis, MN, 55416\n or box number PO Box 727050, Defreestville NY 12144-7050";
    private static final String ZIPCODE_MISSING = "This is some text that contains no zipcode";

    private static final String BILL_OUTLOOK = "bill@outlook.com";
    private static final String BOB_GMAIL = "bob@gmail.com";

    private static final String MINNESOTA_ZIP = "55416";
    private static final String NEWYORK_ZIP = "12144-7050";


    private RegexEntityExtractor extractor;

    @Mock
    private Configuration config;

    @Mock
    private User user;

    @Before
    public void setUp() {
        extractor = new RegexEntityExtractor();
    }

    @Test
    public void testEmailAddressExtraction() throws Exception {
        prepareEmail();

        final TermExtractionResult result = extractor.extract(asStream(EMAIL_TEXT));
        assertNotNull(result);

        final List<TermMention> termMentions = result.getTermMentions();
        assertEquals("Incorrect number of email addresses extracted", 2, termMentions.size());

        TermMention firstTerm = termMentions.get(0);
        assertEquals(EMAIL_TYPE, firstTerm.getOntologyClassUri());
        assertEquals(BOB_GMAIL, firstTerm.getSign());
        assertEquals(59, firstTerm.getStart());
        assertEquals(72, firstTerm.getEnd());

        TermMention secondTerm = termMentions.get(1);
        assertEquals(EMAIL_TYPE, firstTerm.getOntologyClassUri());
        assertEquals(BILL_OUTLOOK, secondTerm.getSign());
        assertEquals(118, secondTerm.getStart());
        assertEquals(134, secondTerm.getEnd());
    }

    @Test
    public void testEmailAddressExtractionWithNewlines() throws Exception {
        prepareEmail();

        final TermExtractionResult result = extractor.extract(asStream(EMAIL_NEW_LINES));
        assertNotNull(result);

        final List<TermMention> termMentions = result.getTermMentions();
        assertEquals("Incorrect number of email addresses extracted", 2, termMentions.size());

        TermMention firstTerm = termMentions.get(0);
        assertEquals(EMAIL_TYPE, firstTerm.getOntologyClassUri());
        assertEquals(BOB_GMAIL, firstTerm.getSign());
        assertEquals(60, firstTerm.getStart());
        assertEquals(73, firstTerm.getEnd());

        TermMention secondTerm = termMentions.get(1);
        assertEquals(EMAIL_TYPE, firstTerm.getOntologyClassUri());
        assertEquals(BILL_OUTLOOK, secondTerm.getSign());
        assertEquals(120, secondTerm.getStart());
        assertEquals(136, secondTerm.getEnd());
    }

    @Test
    public void testNoEmailAddressExtraction() throws Exception {
        prepareEmail();

        final TermExtractionResult result = extractor.extract(asStream(EMAIL_MISSING));
        assertNotNull(result);
        assertTrue(result.getTermMentions().isEmpty());
    }

    @Test
    public void testZipCodeExtraction() throws Exception {
        prepareZipCode();

        final TermExtractionResult result = extractor.extract(asStream(ZIPCODE_TEXT));
        assertNotNull(result);

        final List<TermMention> termMentions = result.getTermMentions();
        assertEquals(2, termMentions.size());

        TermMention firstTerm = termMentions.get(0);
        assertEquals(LOCATION_TYPE, firstTerm.getOntologyClassUri());
        assertEquals(MINNESOTA_ZIP, firstTerm.getSign());
        assertEquals(59, firstTerm.getStart());
        assertEquals(64, firstTerm.getEnd());

        TermMention secondTerm = termMentions.get(1);
        assertEquals(LOCATION_TYPE, firstTerm.getOntologyClassUri());
        assertEquals(NEWYORK_ZIP, secondTerm.getSign());
        assertEquals(111, secondTerm.getStart());
        assertEquals(121, secondTerm.getEnd());
    }

    @Test
    public void testZipCodeExtractionWithNewlines() throws Exception {
        prepareZipCode();

        final TermExtractionResult result = extractor.extract(asStream(ZIPCODE_NEW_LINES));
        assertNotNull(result);

        final List<TermMention> termMentions = result.getTermMentions();
        assertEquals("Incorrect number of email addresses extracted", 2, termMentions.size());

        TermMention firstTerm = termMentions.get(0);
        assertEquals(LOCATION_TYPE, firstTerm.getOntologyClassUri());
        assertEquals(MINNESOTA_ZIP, firstTerm.getSign());
        assertEquals(61, firstTerm.getStart());
        assertEquals(66, firstTerm.getEnd());

        TermMention secondTerm = termMentions.get(1);
        assertEquals(LOCATION_TYPE, firstTerm.getOntologyClassUri());
        assertEquals(NEWYORK_ZIP, secondTerm.getSign());
        assertEquals(114, secondTerm.getStart());
        assertEquals(124, secondTerm.getEnd());
    }

    @Test
    public void testNoZipCodeExtraction() throws Exception {
        prepareZipCode();

        final TermExtractionResult result = extractor.extract(asStream(ZIPCODE_MISSING));
        assertNotNull(result);
        assertTrue(result.getTermMentions().isEmpty());
    }

    private InputStream asStream(final String text) {
        return new ByteArrayInputStream(text.getBytes(Charsets.UTF_8));
    }

    private void prepareEmail() throws IOException {
        when(config.get(RegexEntityExtractor.REGULAR_EXPRESSION)).thenReturn(EMAIL_REG_EX);
        when(config.get(RegexEntityExtractor.ENTITY_TYPE)).thenReturn(EMAIL_TYPE);
        extractor.prepare(config, user);
    }

    private void prepareZipCode() throws IOException {
        when(config.get(RegexEntityExtractor.REGULAR_EXPRESSION)).thenReturn(ZIPCODE_REG_EX);
        when(config.get(RegexEntityExtractor.ENTITY_TYPE)).thenReturn(LOCATION_TYPE);
        extractor.prepare(config, user);
    }
}
