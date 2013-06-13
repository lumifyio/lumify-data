package com.altamiracorp.reddawn.sentenceExtraction;


import com.altamiracorp.reddawn.ucd.artifact.Artifact;
import com.altamiracorp.reddawn.ucd.sentence.Sentence;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

import java.io.IOException;
import java.util.Collection;

public interface SentenceExtractor {
    public Collection<Sentence> extractSentences(Artifact artifact);

    void setup(Mapper<Text,Artifact,Text,Sentence>.Context context) throws IOException;
}
