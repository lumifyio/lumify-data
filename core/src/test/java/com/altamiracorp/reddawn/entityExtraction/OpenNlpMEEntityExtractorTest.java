package com.altamiracorp.reddawn.entityExtraction;

import com.altamiracorp.reddawn.ucd.artifact.ArtifactRowKey;
import com.altamiracorp.reddawn.ucd.artifact.ArtifactType;
import com.altamiracorp.reddawn.ucd.sentence.Sentence;
import com.altamiracorp.reddawn.ucd.sentence.SentenceRowKey;
import com.altamiracorp.reddawn.ucd.term.Term;
import com.altamiracorp.reddawn.ucd.term.TermMention;
import opennlp.tools.namefind.NameFinderME;
import opennlp.tools.namefind.TokenNameFinder;
import opennlp.tools.namefind.TokenNameFinderModel;
import opennlp.tools.tokenize.Tokenizer;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.mapreduce.Mapper.Context;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(JUnit4.class)
public class OpenNlpMEEntityExtractorTest extends BaseExtractorTest{
    private OpenNlpMaximumEntropyEntityExtractor extractor;
    private Context context;

    private String text = "This is a sentence, written by Bob Robertson, who currently makes 2 million "
            + "a year. If by 1:30, you don't know what you are doing, you should go watch CNN and see "
            + "what the latest is on the Benghazi nonsense. I'm 47% sure that this test will pass, but will it?";

    @Before
    public void setUp() throws IOException {
        context = mock(Context.class);
        Configuration config = new Configuration();
        config.set("nlpConfPathPrefix",Thread.currentThread().getContextClassLoader().getResource("fs/").toString());
        when(context.getConfiguration()).thenReturn(config);
        extractor = new OpenNlpMaximumEntropyEntityExtractor();
    }

    @Test
    public void testEntityExtraction() throws Exception {
        extractor.setup(context);
        Collection<Term> terms = extractor.extract(createSentence(text));
        HashMap<String, Term> extractedTerms = new HashMap<String, Term>();
        for (Term term : terms) {
            extractedTerms.put(term.getRowKey().getSign() + "-" + term.getRowKey().getConceptLabel(), term);
        }
        assertTrue("A person wasn't found", extractedTerms.containsKey("Bob Robertson-Person"));
        ArrayList<TermMention> bobRobertsonMentions = new ArrayList<TermMention>(extractedTerms.get("Bob Robertson-Person").getTermMentions());
        assertEquals(1, bobRobertsonMentions.size());
        assertEquals(131, bobRobertsonMentions.get(0).getMentionStart().intValue());
        assertEquals(144, bobRobertsonMentions.get(0).getMentionEnd().intValue());


        assertTrue("A location wasn't found", extractedTerms.containsKey("Benghazi-Location"));
        ArrayList<TermMention> benghaziMentions = new ArrayList<TermMention>(extractedTerms.get("Benghazi-Location").getTermMentions());
        assertEquals(1, benghaziMentions.size());
        assertEquals(289, benghaziMentions.get(0).getMentionStart().intValue());
        assertEquals(297, benghaziMentions.get(0).getMentionEnd().intValue());

        assertTrue("An organization wasn't found", extractedTerms.containsKey("CNN-Organization"));
        ArrayList<TermMention> cnnMentions = new ArrayList<TermMention>(extractedTerms.get("CNN-Organization").getTermMentions());
        assertEquals(1,cnnMentions.size());
        assertEquals(251, cnnMentions.get(0).getMentionStart().intValue());
        assertEquals(254, cnnMentions.get(0).getMentionEnd().intValue());
    }
}
