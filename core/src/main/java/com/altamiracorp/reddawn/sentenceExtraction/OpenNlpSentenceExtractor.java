package com.altamiracorp.reddawn.sentenceExtraction;

import com.altamiracorp.reddawn.cmdline.FileImport;
import com.altamiracorp.reddawn.ucd.artifact.Artifact;
import com.altamiracorp.reddawn.ucd.sentence.Sentence;
import com.altamiracorp.reddawn.ucd.sentence.SentenceData;
import com.altamiracorp.reddawn.ucd.sentence.SentenceMetadata;
import opennlp.tools.sentdetect.SentenceDetector;
import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;
import opennlp.tools.util.Span;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

public class OpenNlpSentenceExtractor implements SentenceExtractor {
    private static final String PATH_PREFIX_CONFIG = "nlpConfPathPrefix";
    private static final String DEFAULT_PATH_PREFIX = "hdfs://";
    private static final String EXTRACTOR_ID = "OpenNLP";
    private SentenceDetector sentenceDetector;
    private static final Logger LOGGER = LoggerFactory.getLogger(FileImport.class.getName());

    @Override
    public void setup(Mapper<Text, Artifact, Text, Sentence>.Context context) throws IOException {
        String pathPrefix = context.getConfiguration().get(PATH_PREFIX_CONFIG, DEFAULT_PATH_PREFIX);
        FileSystem fs = FileSystem.get(context.getConfiguration());
        setSentenceDetector(loadSentenceDetector(fs, pathPrefix));
    }

    @Override
    public Collection<Sentence> extractSentences(Artifact artifact) {
        List<Sentence> sentences = new ArrayList<Sentence>();

        String text = new String(artifact.getContent().getDocExtractedText());
        Span[] sentenceSpans = sentenceDetector.sentPosDetect(text);

        for (Span span : sentenceSpans) {
            Sentence sentence = new Sentence();

            SentenceData data = sentence.getData();
            data.setArtifactId(artifact.getRowKey().toString());
            data.setStart(Long.valueOf(span.getStart()));
            data.setEnd(Long.valueOf(span.getEnd()));
            int spanLength = span.getEnd() - span.getStart();
            data.setText(text.substring(span.getStart(), span.getStart() + spanLength));

            SentenceMetadata metaData = sentence.getMetadata();
            metaData.setContentHash(data.getText())
                    .setDate(getDate().getTime())
                    .setExtractorId(EXTRACTOR_ID)
                    .setArtifactSubject(artifact.getGenericMetadata().getSubject())
                    .setArtifactType(artifact.getType());

            if (artifact.getGenericMetadata().getAuthor() != null) {
                metaData.setAuthor(artifact.getGenericMetadata().getAuthor());
            }

            if (artifact.getContent().getSecurity() != null) {
                metaData.setSecurityMarking(artifact.getContent().getSecurity());
            }

            sentences.add(sentence);
        }

        return sentences;
    }

    protected void setSentenceDetector(SentenceDetector sentenceDetector) {
        this.sentenceDetector = sentenceDetector;
    }

    protected Date getDate() {
        return new Date();
    }

    protected SentenceDetector loadSentenceDetector(FileSystem fs, String pathPrefix) throws IOException {
        Path sentenceHdfsPath = new Path(pathPrefix + "/conf/opennlp/en-sent.bin");
        LOGGER.debug("Using sentence detector at: " + sentenceHdfsPath);
        InputStream sentenceModelInputStream = fs.open(sentenceHdfsPath);

        try {
            SentenceModel sentenceModel = new SentenceModel(sentenceModelInputStream);
            return new SentenceDetectorME(sentenceModel);
        } finally {
            sentenceModelInputStream.close();
        }
    }

}
