package com.altamiracorp.reddawn.sentenceExtraction;

import com.altamiracorp.reddawn.ucd.artifact.Artifact;
import com.altamiracorp.reddawn.ucd.sentence.Sentence;
import com.altamiracorp.reddawn.ucd.sentence.SentenceData;
import com.altamiracorp.reddawn.ucd.sentence.SentenceMetadata;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

public class OpenNlpSentenceExtractor implements SentenceExtractor {

    private static final String EXTRACTOR_ID = "OpenNLP";

    @Override
    public Collection<Sentence> extractSentences(Artifact artifact) {
        ArrayList<Sentence> result = new ArrayList<Sentence>();
        Sentence sentence = new Sentence();
        SentenceData data = sentence.getData();
        data.setArtifactId(artifact.getRowKey().toString());
        data.setStart(0L);
        data.setEnd(100L);
        data.setText("Hello World!");

        SentenceMetadata metaData = sentence.getMetadata();

        if (artifact.getGenericMetadata().getAuthor() != null) {
            metaData.setAuthor(artifact.getGenericMetadata().getAuthor());
        }

        metaData.setContentHash("Hello World!".getBytes());
        metaData.setDate(new Date().getTime());
        metaData.setExtractorId(EXTRACTOR_ID);
        metaData.setSecurityMarking("U");
        result.add(sentence);
        return result;
    }

    @Override
    public void setup(Mapper<Text, Artifact, Text, Sentence>.Context context) {

    }
}
