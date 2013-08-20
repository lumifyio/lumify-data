package com.altamiracorp.reddawn.entityExtraction;

import com.altamiracorp.reddawn.ucd.artifact.ArtifactRowKey;
import com.altamiracorp.reddawn.ucd.artifact.ArtifactType;
import com.altamiracorp.reddawn.ucd.sentence.Sentence;
import com.altamiracorp.reddawn.ucd.sentence.SentenceRowKey;
import com.altamiracorp.reddawn.ucd.term.Term;
import com.altamiracorp.reddawn.ucd.term.TermMention;
import opennlp.tools.dictionary.Dictionary;
import opennlp.tools.namefind.DictionaryNameFinder;
import opennlp.tools.namefind.TokenNameFinder;
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
import java.net.URL;
import java.util.*;
import java.util.Map.Entry;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(JUnit4.class)
public class OpenNlpDictionaryEntityExtractorTest extends BaseExtractorTest{

    private OpenNlpDictionaryEntityExtractor extractor;
    private Context context;

    private String text = "This is a sentence that is going to tell you about a guy named "
            + "Bob Robertson who lives in Boston, MA and works for a company called Altamira Corporation";

    @Before
    public void setUp() throws IOException {
        context = mock(Context.class);
        Configuration config = new Configuration();
        config.set("nlpConfPathPrefix",Thread.currentThread().getContextClassLoader().getResource("fs/").toString());
        when(context.getConfiguration()).thenReturn(config);
        extractor = new OpenNlpDictionaryEntityExtractor();
    }

    @Test
    public void testEntityExtraction() throws Exception {
        extractor.setup(context);
        Collection<Term> terms = extractor.extract(createSentence(text));
        List<String> extractedTerms = new ArrayList<String>();
        for (Term term : terms) {
            extractedTerms.add(term.getRowKey().getSign() + "-" + term.getRowKey().getConceptLabel());
        }
        validateOutput(extractedTerms);
    }

    @Test
    public void testEntityExtractionSetsMentionRelativeToArtifactNotSentence() throws Exception {
        extractor.setup(context);;
        Collection<Term> terms = extractor.extract(createSentence(text));
        boolean found = false;
        for (Term term : terms) {
            if (term.getRowKey().toString().equals("Bob Robertson\u001FOpenNlpDictionary\u001FPerson")) {
                found = true;
                assertEquals((Long)163L, term.getTermMentions().get(0).getMentionStart());
                assertEquals((Long)176L, term.getTermMentions().get(0).getMentionEnd());
                break;
            }
        }
        assertTrue("Expected name not found!",found);
    }

    @Test
    public void testEntityExtractionSetsSecurityMarking() throws Exception {
        extractor.setup(context);
        Sentence sentence = createSentence(text);
        sentence.getMetadata().setSecurityMarking("U");
        Collection<Term> terms = extractor.extract(sentence);
        boolean found = false;
        for (Term term : terms) {
            if (term.getRowKey().toString().equals("Bob Robertson\u001FOpenNlpDictionary\u001FPerson")) {
                found = true;
                assertEquals("U", term.getTermMentions().get(0).getSecurityMarking());
                break;
            }
        }
        assertTrue("Expected name not found!",found);
    }

    private void validateOutput(List<String> terms) {
        assertTrue("A person wasn't found", terms.contains("Bob Robertson-Person"));
        assertTrue("A location wasn't found", terms.contains("Boston , MA-Location"));
        assertTrue("An organization wasn't found", terms.contains("Altamira Corporation-Organization"));
    }

}
