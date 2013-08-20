package com.altamiracorp.reddawn.location;

import com.altamiracorp.reddawn.ucd.artifact.Artifact;
import com.altamiracorp.reddawn.ucd.term.TermAndTermMention;
import org.apache.hadoop.mapreduce.Mapper;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

public interface ArtifactLocationExtractor {
    void setup(Mapper.Context context) throws IOException;

    void extract(Artifact artifact, List<TermAndTermMention> termAndTermMentions) throws Exception;
}
