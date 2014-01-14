package com.altamiracorp.lumify.storm.term.extraction;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
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
public class PhoneNumberExtractorTest {
    private static final String PHONE_TEXT = "This terrorist's phone number is 410-678-2230, and his best buddy's phone number is +44 (0)207 437 0478";
    private static final String PHONE_NEW_LINES = "This terrorist's phone\n number is 410-678-2230, and his best buddy's phone number\n is +44 (0)207 437 0478";
    private static final String PHONE_MISSING = "This is a sentence without any phone numbers in it.";


    @Mock
    private Configuration config;

    @Mock
    private User user;

    private PhoneNumberExtractor extractor;


    @Before
    public void setUp() {
        when(config.get(PhoneNumberExtractor.DEFAULT_REGION_CODE, PhoneNumberExtractor.DEFAULT_DEFAULT_REGION_CODE)).thenReturn(PhoneNumberExtractor.DEFAULT_DEFAULT_REGION_CODE);
        extractor = new PhoneNumberExtractor();
        extractor.prepare(config, user);
    }

    @Test
    public void testPhoneNumberExtraction() throws Exception {
        final TermExtractionResult result = extractor.extract(asStream(PHONE_TEXT));
        assertNotNull(result);

        final List<TermMention> termMentions = result.getTermMentions();

        assertEquals("Incorrect number of phone numbers extracted", 2, termMentions.size());
        TermMention firstTerm = termMentions.get(0);
        assertEquals("First phone number not correctly extracted", "+14106782230", firstTerm.getSign());
        assertEquals(33, firstTerm.getStart());
        assertEquals(45, firstTerm.getEnd());

        TermMention secondTerm = termMentions.get(1);
        assertEquals("Second phone number not correctly extracted", "+442074370478", secondTerm.getSign());
        assertEquals(84, secondTerm.getStart());
        assertEquals(103, secondTerm.getEnd());
    }

    @Test
    public void testPhoneNumberExtractionWithNewlines() throws Exception {
        final TermExtractionResult result = extractor.extract(asStream(PHONE_NEW_LINES));
        assertNotNull(result);

        final List<TermMention> termMentions = result.getTermMentions();

        assertEquals("Incorrect number of phone numbers extracted", 2, termMentions.size());
        TermMention firstTerm = termMentions.get(0);
        assertEquals("First phone number not correctly extracted", "+14106782230", firstTerm.getSign());
        assertEquals(34, firstTerm.getStart());
        assertEquals(46, firstTerm.getEnd());

        TermMention secondTerm = termMentions.get(1);
        assertEquals("Second phone number not correctly extracted", "+442074370478", secondTerm.getSign());
        assertEquals(86, secondTerm.getStart());
        assertEquals(105, secondTerm.getEnd());
    }

    @Test
    public void testNegativePhoneNumberExtraction() throws Exception {
        final TermExtractionResult result = extractor.extract(asStream(PHONE_MISSING));
        assertNotNull(result);

        assertTrue("Phone number extracted when there were no phone numbers", result.getTermMentions().isEmpty());
    }

    private InputStream asStream(final String text) {
        return new ByteArrayInputStream(text.getBytes(Charsets.UTF_8));
    }
}
