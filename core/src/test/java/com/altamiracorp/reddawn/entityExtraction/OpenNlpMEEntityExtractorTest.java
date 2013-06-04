package com.altamiracorp.reddawn.entityExtraction;

import com.altamiracorp.reddawn.ucd.model.Term;
import com.altamiracorp.reddawn.ucd.model.artifact.ArtifactKey;
import com.altamiracorp.reddawn.ucd.model.terms.TermMetadata;
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
        ArtifactKey key = ArtifactKey.newBuilder()
                .docArtifactBytes(text.getBytes()).build();
        Collection<Term> terms = extractor.extract(key, text);
        HashMap<String, Term> extractedTerms = new HashMap<String, Term>();
        for (Term term : terms) {
            extractedTerms.put(term.getKey().getSign() + "-" + term.getKey().getConcept(), term);
        }
        assertTrue("A person wasn't found", extractedTerms.containsKey("bob robertson-person"));
        ArrayList<TermMetadata> bobRobertsonMetadatas = new ArrayList<TermMetadata>(extractedTerms.get("bob robertson-person").getMetadata());
        assertEquals(1, bobRobertsonMetadatas.size());
        assertEquals(31, bobRobertsonMetadatas.get(0).getMention().getStart());
        assertEquals(44, bobRobertsonMetadatas.get(0).getMention().getEnd());

        assertTrue("A date wasn't found", extractedTerms.containsKey("a year-date"));
        assertTrue("Money wasn't found", extractedTerms.containsKey("2 million-money"));

        assertTrue("A location wasn't found", extractedTerms.containsKey("benghazi-location"));
        ArrayList<TermMetadata> benghaziMetadatas = new ArrayList<TermMetadata>(extractedTerms.get("benghazi-location").getMetadata());
        assertEquals(1, benghaziMetadatas.size());
        assertEquals(189, benghaziMetadatas.get(0).getMention().getStart());
        assertEquals(197, benghaziMetadatas.get(0).getMention().getEnd());

        assertTrue("An organization wasn't found", extractedTerms.containsKey("cnn-organization"));
        assertTrue("A percentage wasn't found", extractedTerms.containsKey("47-percentage"));
        assertTrue("A time wasn't found", extractedTerms.containsKey("1:30-time"));
    }
}
