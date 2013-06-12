package com.altamiracorp.reddawn.sentenceExtraction;

import com.altamiracorp.reddawn.ucd.artifact.Artifact;
import com.altamiracorp.reddawn.ucd.artifactTermIndex.ArtifactTermIndex;
import com.altamiracorp.reddawn.ucd.sentence.Sentence;
import com.altamiracorp.reddawn.ucd.term.Term;
import com.altamiracorp.reddawn.ucd.term.TermMention;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

public class OpenNlpSentenceExtractor {
    protected void setup(Mapper.Context context) throws IOException, InterruptedException {

    }

    public void map(Text rowKey, Artifact artifact, Mapper.Context context) throws IOException, InterruptedException {
    }

    private Collection<Sentence> extractSentences(Artifact artifact) {
        return new ArrayList<Sentence>();
    }
}
