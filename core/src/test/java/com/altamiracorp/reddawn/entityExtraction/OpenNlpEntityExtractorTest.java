package com.altamiracorp.reddawn.entityExtraction;

import com.altamiracorp.reddawn.ucd.artifact.ArtifactRowKey;
import com.altamiracorp.reddawn.ucd.sentence.Sentence;
import com.altamiracorp.reddawn.ucd.sentence.SentenceRowKey;
import com.altamiracorp.reddawn.ucd.term.Term;
import com.altamiracorp.reddawn.ucd.term.TermMention;
import opennlp.tools.dictionary.*;
import opennlp.tools.namefind.DictionaryNameFinder;
import opennlp.tools.namefind.TokenNameFinder;
import opennlp.tools.tokenize.Tokenizer;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;
import org.apache.hadoop.mapreduce.Mapper;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.Dictionary;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class OpenNlpEntityExtractorTest {
    private OpenNlpDictionaryEntityExtractor extractor;
    private Mapper.Context context;
    private String tokenizerModelFile = "en-token.bin";
    private Map<String, String> finderDictionaryFiles = new HashMap<String, String>();

    private String text = "This is a sentence that is going to tell you about a guy named "
            + "Bob Robertson who lives in Boston, MA and works for a company called Altamira Corporation";

    @Before
    public void setUp() throws IOException {
        setUpDictionaryFiles();
        context = mock(Mapper.Context.class);
        extractor = new OpenNlpDictionaryEntityExtractor() {

            @Override
            public void setup(Mapper.Context context) throws IOException {
                setFinders(loadFinders());
                setTokenizer(loadTokenizer());
            }

            @Override
            protected List<TokenNameFinder> loadFinders() throws IOException {
                List<TokenNameFinder> finders = new ArrayList<TokenNameFinder>();
                for (Map.Entry<String, String> finderDictionaryFileEntry : finderDictionaryFiles.entrySet()) {
                    InputStream finderDictionaryIn = Thread
                            .currentThread()
                            .getContextClassLoader()
                            .getResourceAsStream(finderDictionaryFileEntry.getValue());
                    opennlp.tools.dictionary.Dictionary finderDictionary = new opennlp.tools.dictionary.Dictionary(finderDictionaryIn);
                    finders.add(new DictionaryNameFinder(finderDictionary, finderDictionaryFileEntry.getKey()));
                }

                return finders;
            }

            @Override
            protected Tokenizer loadTokenizer() throws IOException {
                InputStream tokenizerModelIn = Thread.currentThread()
                        .getContextClassLoader()
                        .getResourceAsStream(tokenizerModelFile);
                TokenizerModel tokenizerModel = new TokenizerModel(tokenizerModelIn);
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
        sentence.getData().setArtifactId(artifactRowKey.toString());
        sentence.getData().setText(text);
        sentence.getData().setStart(0L);
        sentence.getData().setEnd(100L);
        Collection<Term> terms = extractor.extract(sentence);
        List<String> extractedTerms = new ArrayList<String>();
        for (Term term : terms) {
            extractedTerms.add(term.getRowKey().getSign() + "-" + term.getRowKey().getConceptLabel());
        }
        validateOutput(extractedTerms);
    }

    @Test
    public void testEntityExtractionSetsMentionRelativeToArtifactNotSentence() throws Exception {
        extractor.setup(context);
        ArtifactRowKey artifactRowKey = ArtifactRowKey.build(text.getBytes());
        SentenceRowKey sentenceRowKey = new SentenceRowKey(artifactRowKey.toString(), 100, 200);
        Sentence sentence = new Sentence(sentenceRowKey);
        sentence.getData().setArtifactId(artifactRowKey.toString());
        sentence.getData().setText(text);
        sentence.getData().setStart(100L);
        sentence.getData().setEnd(200L);
        Collection<Term> terms = extractor.extract(sentence);
        Term firstTerm = terms.iterator().next();
        assertEquals("altamira corporation\u001FOpenNlpDictionary\u001Forganization", firstTerm.getRowKey().toString());
        TermMention firstTermMention = firstTerm.getTermMentions().get(0);
        assertEquals((Long)232L, firstTermMention.getMentionStart());
        assertEquals((Long)252L, firstTermMention.getMentionEnd());
    }

    @Test
    public void testEntityExtractionSetsSecurityMarking() throws Exception {
        extractor.setup(context);
        ArtifactRowKey artifactRowKey = ArtifactRowKey.build(text.getBytes());
        SentenceRowKey sentenceRowKey = new SentenceRowKey(artifactRowKey.toString(), 100, 200);
        Sentence sentence = new Sentence(sentenceRowKey);
        sentence.getData().setArtifactId(artifactRowKey.toString());
        sentence.getData().setText(text);
        sentence.getData().setStart(100L);
        sentence.getData().setEnd(200L);
        sentence.getMetadata().setSecurityMarking("U");
        Collection<Term> terms = extractor.extract(sentence);
        Term firstTerm = terms.iterator().next();
        assertEquals("altamira corporation\u001FOpenNlpDictionary\u001Forganization", firstTerm.getRowKey().toString());
        TermMention firstTermMention = firstTerm.getTermMentions().get(0);
        assertEquals("U", firstTermMention.getSecurityMarking());
    }

    @Test
    public void testLoadTokenizer() throws Exception {
        Tokenizer mockedTokenizer = mock(Tokenizer.class);
        extractor.setTokenizer(mockedTokenizer);
        extractor.loadTokenizer();
        OpenNlpEntityExtractor mockedExtractor = mock(OpenNlpEntityExtractor.class);
        mockedExtractor.loadTokenizer();
        verify(mockedExtractor).loadTokenizer();

    }

    private void validateOutput(List<String> terms) {
        assertTrue("A person wasn't found", terms.contains("bob robertson-person"));
        assertTrue("A location wasn't found", terms.contains("boston , ma-location"));
        assertTrue("An organization wasn't found", terms.contains("altamira corporation-organization"));

    }

    private void setUpDictionaryFiles() {
        finderDictionaryFiles.put("location", "en-ner-location.dict");
        finderDictionaryFiles.put("person", "en-ner-person.dict");
        finderDictionaryFiles.put("organization", "en-ner-organization.dict");
    }
}
