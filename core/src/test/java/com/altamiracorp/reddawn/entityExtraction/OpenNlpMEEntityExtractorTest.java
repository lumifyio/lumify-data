package com.altamiracorp.reddawn.entityExtraction;

import com.altamiracorp.reddawn.ucd.artifact.ArtifactRowKey;
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

@RunWith(JUnit4.class)
public class OpenNlpMEEntityExtractorTest {
    private OpenNlpMaximumEntropyEntityExtractor extractor;
    private Context context;
    private String tokenizerModelFile = "en-token.bin";
    private String[] finderModelFiles = new String[]{"en-ner-date.bin",
            "en-ner-money.bin", "en-ner-location.bin",
            "en-ner-organization.bin", "en-ner-percentage.bin",
            "en-ner-person.bin", "en-ner-time.bin"};

    private String text = "This is a sentence, written by Bob Robertson, who currently makes 2 million "
            + "a year. If by 1:30, you don't know what you are doing, you should go watch CNN and see "
            + "what the latest is on the Benghazi nonsense. I'm 47% sure that this test will pass, but will it?";

    @Before
    public void setUp() throws IOException {
        context = mock(Context.class);
        extractor = new OpenNlpMaximumEntropyEntityExtractor() {

            @Override
            public void setup(Context context) throws IOException {
                setFinders(loadFinders());
                setTokenizer(loadTokenizer());
            }

            @Override
            protected List<TokenNameFinder> loadFinders() throws IOException {
                List<TokenNameFinder> finders = new ArrayList<TokenNameFinder>();
                for (String finderModelFile : finderModelFiles) {
                    InputStream finderModelIn = Thread.currentThread()
                            .getContextClassLoader()
                            .getResourceAsStream(finderModelFile);
                    TokenNameFinderModel finderModel = new TokenNameFinderModel(
                            finderModelIn);
                    finders.add(new NameFinderME(finderModel));
                }

                return finders;
            }

            @Override
            protected Tokenizer loadTokenizer() throws IOException {
                InputStream tokenizerModelIn = Thread.currentThread()
                        .getContextClassLoader()
                        .getResourceAsStream(tokenizerModelFile);
                TokenizerModel tokenizerModel = new TokenizerModel(
                        tokenizerModelIn);
                return new TokenizerME(tokenizerModel);
            }
        };
    }

    @Test
    public void testEntityExtraction() throws Exception {
        extractor.setup(context);
        ArtifactRowKey artifactRowKey = ArtifactRowKey.build(text.getBytes());
        SentenceRowKey sentenceRowKey = new SentenceRowKey(artifactRowKey.toString(), 0, 100);
        Sentence sentence = new Sentence(sentenceRowKey);
        sentence.getData().setText(text);
        sentence.getData().setStart(0L);
        sentence.getData().setEnd(100L);
        Collection<Term> terms = extractor.extract(sentence);
        HashMap<String, Term> extractedTerms = new HashMap<String, Term>();
        for (Term term : terms) {
            extractedTerms.put(term.getRowKey().getSign() + "-" + term.getRowKey().getConceptLabel(), term);
        }
        assertTrue("A person wasn't found", extractedTerms.containsKey("bob robertson-person"));
        ArrayList<TermMention> bobRobertsonMentions = new ArrayList<TermMention>(extractedTerms.get("bob robertson-person").getTermMentions());
        assertEquals(1, bobRobertsonMentions.size());
        assertEquals(31, bobRobertsonMentions.get(0).getMentionStart().intValue());
        assertEquals(44, bobRobertsonMentions.get(0).getMentionEnd().intValue());

        assertTrue("A date wasn't found", extractedTerms.containsKey("a year-date"));
        assertTrue("Money wasn't found", extractedTerms.containsKey("2 million-money"));

        assertTrue("A location wasn't found", extractedTerms.containsKey("benghazi-location"));
        ArrayList<TermMention> benghaziMentions = new ArrayList<TermMention>(extractedTerms.get("benghazi-location").getTermMentions());
        assertEquals(1, benghaziMentions.size());
        assertEquals(189, benghaziMentions.get(0).getMentionStart().intValue());
        assertEquals(197, benghaziMentions.get(0).getMentionEnd().intValue());

        assertTrue("An organization wasn't found", extractedTerms.containsKey("cnn-organization"));
        assertTrue("A percentage wasn't found", extractedTerms.containsKey("47-percentage"));
        assertTrue("A time wasn't found", extractedTerms.containsKey("1:30-time"));
    }
}
