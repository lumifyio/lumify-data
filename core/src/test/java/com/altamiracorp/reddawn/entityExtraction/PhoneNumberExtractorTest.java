package com.altamiracorp.reddawn.entityExtraction;

import com.altamiracorp.reddawn.model.termMention.TermMention;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.mapreduce.Mapper.Context;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.Collection;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

@RunWith(JUnit4.class)
public class PhoneNumberExtractorTest extends BaseExtractorTest {
    private Context context;
    private EntityExtractor extractor;

    private String textWith = "This terrorist's phone number is 410-678-2230, and his best buddy's phone number is +44 (0)207 437 0478";
    private String textWithNewLines = "This terrorist's phone\n number is 410-678-2230, and his best buddy's phone number\n is +44 (0)207 437 0478";
    private String textWithout = "This is a sentence without any phone numbers in it.";


    @Before
    public void setUp() {
        context = Mockito.mock(Context.class);
        when(context.getConfiguration()).thenReturn(new Configuration());
        extractor = new PhoneNumberExtractor();
    }

    @Test
    public void testPhoneNumberExtraction() throws Exception {
        extractor.setup(context);
        ArrayList<TermMention> termList = new ArrayList<TermMention>(extractor.extract(createArtifact(textWith), textWith));

        assertTrue("Incorrect number of phone numbers extracted", termList.size() == 2);
        TermMention firstTerm = termList.get(0);
        assertEquals("First phone number not correctly extracted", "+14106782230\u001Flibphonenumber\u001FPhone Number", firstTerm.getRowKey().toString());
        assertEquals(133L, firstTerm.getRowKey().getStartOffset());
        assertEquals(145L, firstTerm.getRowKey().getEndOffset());

        TermMention secondTerm = termList.get(1);
        assertEquals("Second phone number not correctly extracted", "+442074370478\u001Flibphonenumber\u001FPhone Number", termList.get(1).getRowKey().toString());
        assertEquals(184L, secondTerm.getRowKey().getStartOffset());
        assertEquals(203L, secondTerm.getRowKey().getEndOffset());
    }

    @Test
    public void testPhoneNumberExtractionWithNewlines() throws Exception {
        extractor.setup(context);
        ArrayList<TermMention> termList = new ArrayList<TermMention>(extractor.extract(createArtifact(textWithNewLines), textWithNewLines));

        assertTrue("Incorrect number of phone numbers extracted", termList.size() == 2);
        TermMention firstTerm = termList.get(0);
        assertEquals("First phone number not correctly extracted", "+14106782230\u001Flibphonenumber\u001FPhone Number", firstTerm.getRowKey().toString());
        assertEquals(134L, firstTerm.getRowKey().getStartOffset());
        assertEquals(146L, firstTerm.getRowKey().getEndOffset());

        TermMention secondTerm = termList.get(1);
        assertEquals("Second phone number not correctly extracted", "+442074370478\u001Flibphonenumber\u001FPhone Number", termList.get(1).getRowKey().toString());
        assertEquals(186L, secondTerm.getRowKey().getStartOffset());
        assertEquals(205L, secondTerm.getRowKey().getEndOffset());
    }

    @Test
    public void testNegativePhoneNumberExtraction() throws Exception {
        extractor.setup(context);
        Collection<TermMention> terms = extractor.extract(createArtifact(textWithout), textWithout);

        assertNotNull(terms);
        assertTrue("Phone number extracted when there were no phone numbers", terms.isEmpty());
    }
}
