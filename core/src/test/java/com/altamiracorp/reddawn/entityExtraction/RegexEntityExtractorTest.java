package com.altamiracorp.reddawn.entityExtraction;

import com.altamiracorp.reddawn.ucd.term.Term;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.mapreduce.Mapper.Context;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.Collection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

@RunWith(JUnit4.class)
public class RegexEntityExtractorTest extends BaseExtractorTest {

    private EntityExtractor extractor;
    private Context context;

    private String textWith = "This is some text that contains an e-mail address for Bob, bob@gmail.com and an e-mail address for Bob's friend Bill, bill@outlook.com.";
    private String textWithNewlines = "This is some text that contains\n an e-mail address for Bob, bob@gmail.com and an e-mail address\n for Bob's friend Bill, bill@outlook.com.";
    private String textWithout = "This is some text that contains no e-mail address";

    @Before
    public void setUp() {
        context = Mockito.mock(Context.class);
        Configuration config = new Configuration();
        config.set("regularExpression", "(?i)\\b[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,4}\\b");
        config.set("entityType", "emailAddress");
        when(context.getConfiguration()).thenReturn(config);

        extractor = new RegexEntityExtractor();
    }

    @Test
    public void testRegularExpressionExtraction() throws Exception {
        extractor.setup(context);
        ArrayList<Term> terms = new ArrayList<Term>(extractor.extract(createSentence(textWith)));
        assertEquals("Not enough terms extracted", 2, terms.size());
        boolean found = false;
        for (Term term : terms) {
            if (term.getRowKey().toString().equals("bob@gmail.com\u001FRegularExpression\u001FEmailAddress")) {
                found = true;
                assertEquals((Long) 159L, term.getTermMentions().get(0).getMentionStart());
                assertEquals((Long) 172L, term.getTermMentions().get(0).getMentionEnd());
            }
        }
        assertTrue("Expected entity not found", found);
    }

    @Test
    public void testRegularExpressionExtractionWithNewlines() throws Exception {
        extractor.setup(context);
        ArrayList<Term> terms = new ArrayList<Term>(extractor.extract(createSentence(textWithNewlines)));
        assertEquals("Not enough terms extracted", 2, terms.size());
        boolean found = false;
        for (Term term : terms) {
            if (term.getRowKey().toString().equals("bob@gmail.com\u001FRegularExpression\u001FEmailAddress")) {
                found = true;
                assertEquals((Long) 160L, term.getTermMentions().get(0).getMentionStart());
                assertEquals((Long) 173L, term.getTermMentions().get(0).getMentionEnd());
            }
        }
        assertTrue("Expected entity not found", found);
    }

    @Test
    public void testNegativeRegularExpressionExtraction () throws Exception {
        extractor.setup(context);
        Collection<Term> terms = extractor.extract(createSentence(textWithout));
        assertTrue(terms.isEmpty());
    }
}

