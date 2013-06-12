package com.altamiracorp.reddawn.sentenceExtraction;

import com.altamiracorp.reddawn.ucd.artifact.Artifact;
import com.altamiracorp.reddawn.ucd.sentence.Sentence;
import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;

import static org.junit.Assert.*;

@RunWith(JUnit4.class)
public class OpenNlpSentenceExtractorTest {
    private OpenNlpSentenceExtractor extractor;
    private String sentenceModelFile = "en-sent.bin";
    private Date createDate = new Date();

    @Before
    public void setUp() throws IOException {
        extractor = new OpenNlpSentenceExtractor() {
            @Override
            public void setup(Mapper<Text, Artifact, Text, Sentence>.Context context) throws IOException {
                InputStream sentenceModelIn = Thread.currentThread()
                        .getContextClassLoader()
                        .getResourceAsStream(sentenceModelFile);
                SentenceModel sentenceModel = new SentenceModel(sentenceModelIn);
                setSentenceDetector(new SentenceDetectorME(sentenceModel));
            }

            @Override
            protected Date getDate() {
                return createDate;
            }
        };
        extractor.setup(null);
    }

    @Test
    public void testExtractionOfTwoSentences() {
        Artifact artifact = new Artifact("urn:sha256:abcd");
        artifact.getGenericMetadata().setAuthor("author");
        artifact.getContent().setSecurity("U");
        String text = "This is some text. It has two sentences.";
        artifact.getContent().setDocExtractedText(text.getBytes());

        Collection<Sentence> sentences = extractor.extractSentences(artifact);
        assertEquals(2, sentences.size());

        Iterator<Sentence> iterator = sentences.iterator();

        Sentence sentence = iterator.next();
        assertEquals("urn:sha256:abcd:0000000000000018:0000000000000000", sentence.getRowKey().toString());
        assertEquals("urn:sha256:abcd", sentence.getData().getArtifactId());
        assertEquals(Long.valueOf(0), sentence.getData().getStart());
        assertEquals(Long.valueOf(18), sentence.getData().getEnd());
        assertEquals("This is some text.", sentence.getData().getText());
        assertEquals("author", sentence.getMetadata().getAuthor());
        assertEquals((Long)createDate.getTime(), sentence.getMetadata().getDate());
        assertEquals("OpenNLP", sentence.getMetadata().getExtractorId());
        assertEquals("U", sentence.getMetadata().getSecurityMarking());

        sentence = iterator.next();
        assertEquals("urn:sha256:abcd", sentence.getData().getArtifactId());
        assertEquals(Long.valueOf(19), sentence.getData().getStart());
        assertEquals(Long.valueOf(40), sentence.getData().getEnd());
        assertEquals("It has two sentences.", sentence.getData().getText());
        assertEquals("author", sentence.getMetadata().getAuthor());
        assertEquals((Long)createDate.getTime(), sentence.getMetadata().getDate());
        assertEquals("OpenNLP", sentence.getMetadata().getExtractorId());
        assertEquals("U", sentence.getMetadata().getSecurityMarking());
    }
}
