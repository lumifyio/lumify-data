package com.altamiracorp.reddawn.entityExtraction;

import com.altamiracorp.reddawn.ucd.term.Term;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.mapreduce.Mapper.Context;

import org.junit.Test;
import org.junit.Before;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import org.junit.runners.JUnit4;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.Collection;

@RunWith(JUnit4.class)
public class PhoneNumberExtractorTest extends BaseExtractorTest{
    private Context context;
    private EntityExtractor extractor;

    private String textWith = "This terrorist's phone number is 410-678-2230, and his best buddy's phone number is +44 (0)207 437 0478";
    private String textWithNewLines = "This terrorist's phone\n number is 410-678-2230, and his best buddy's phone number\n is +44 (0)207 437 0478";
    private String textWithout = "This is a sentence without any phone numbers in it.";


    @Before
    public void setUp () {
        context = Mockito.mock(Context.class);
        when(context.getConfiguration()).thenReturn(new Configuration());
        extractor = new PhoneNumberExtractor();
    }

    @Test
    public void testPhoneNumberExtraction () throws Exception {
        extractor.setup(context);
        ArrayList<Term> termList = new ArrayList<Term>(extractor.extract(createSentence(textWith)));

        assertTrue("Incorrect number of phone numbers extracted", termList.size() == 2);
        Term firstTerm = termList.get(0);
        assertEquals("First phone number not correctly extracted", "+14106782230\u001Flibphonenumber\u001FPhone Number", firstTerm.getRowKey().toString());
        assertEquals((Long)133L,firstTerm.getTermMentions().get(0).getMentionStart());
        assertEquals((Long)145L,firstTerm.getTermMentions().get(0).getMentionEnd());

        Term secondTerm = termList.get(1);
        assertEquals("Second phone number not correctly extracted", "+442074370478\u001Flibphonenumber\u001FPhone Number", termList.get(1).getRowKey().toString());
        assertEquals((Long)184L,secondTerm.getTermMentions().get(0).getMentionStart());
        assertEquals((Long)203L,secondTerm.getTermMentions().get(0).getMentionEnd());
    }

    @Test
    public void testPhoneNumberExtractionWithNewlines () throws Exception {
        extractor.setup(context);
        ArrayList<Term> termList = new ArrayList<Term>(extractor.extract(createSentence(textWithNewLines)));

        assertTrue("Incorrect number of phone numbers extracted", termList.size() == 2);
        Term firstTerm = termList.get(0);
        assertEquals("First phone number not correctly extracted", "+14106782230\u001Flibphonenumber\u001FPhone Number", firstTerm.getRowKey().toString());
        assertEquals((Long)134L,firstTerm.getTermMentions().get(0).getMentionStart());
        assertEquals((Long)146L,firstTerm.getTermMentions().get(0).getMentionEnd());

        Term secondTerm = termList.get(1);
        assertEquals("Second phone number not correctly extracted", "+442074370478\u001Flibphonenumber\u001FPhone Number", termList.get(1).getRowKey().toString());
        assertEquals((Long)186L,secondTerm.getTermMentions().get(0).getMentionStart());
        assertEquals((Long)205L,secondTerm.getTermMentions().get(0).getMentionEnd());
    }

    @Test
    public void testNegativePhoneNumberExtraction () throws Exception {
        extractor.setup(context);
        Collection<Term> terms = extractor.extract(createSentence(textWithout));

        assertNotNull(terms);
        assertTrue("Phone number extracted when there were no phone numbers", terms.isEmpty());
    }
}
