package com.altamiracorp.reddawn.entityExtraction;

import com.altamiracorp.reddawn.ucd.artifact.ArtifactRowKey;
import com.altamiracorp.reddawn.ucd.term.Term;
import org.apache.hadoop.mapreduce.Mapper;

import java.io.IOException;
import java.util.Collection;

public interface EntityExtractor {
    void setup(Mapper.Context context) throws IOException;

    Collection<Term> extract(ArtifactRowKey artifactKey, String text) throws Exception;
}
