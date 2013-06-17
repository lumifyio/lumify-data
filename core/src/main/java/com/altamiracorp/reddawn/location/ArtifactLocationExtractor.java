package com.altamiracorp.reddawn.location;

import com.altamiracorp.reddawn.ucd.artifact.Artifact;
import com.altamiracorp.reddawn.ucd.term.Term;
import org.apache.hadoop.mapreduce.Mapper;

import java.io.IOException;
import java.util.Collection;

public interface ArtifactLocationExtractor {
    void setup(Mapper.Context context) throws IOException;

    Collection<Artifact> extract(Term term) throws Exception;
}
