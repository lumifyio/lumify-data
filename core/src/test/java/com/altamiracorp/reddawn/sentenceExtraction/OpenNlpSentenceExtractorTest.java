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

import static org.junit.Assert.assertEquals;

@RunWith(JUnit4.class)
public class OpenNlpSentenceExtractorTest {
    private OpenNlpSentenceExtractor extractor;
    private String sentenceModelFile = "en-sent.bin";

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
        };
        extractor.setup(null);
    }

    @Test
    public void testExtractionOfTwoSentences() {
        Artifact artifact = new Artifact("rowKey");
        String text = "This is some text. It has two sentences.";
        artifact.getContent().setDocExtractedText(text.getBytes());

        Collection<Sentence> sentences = extractor.extractSentences(artifact);
        assertEquals(2, sentences.size());
    }
}
