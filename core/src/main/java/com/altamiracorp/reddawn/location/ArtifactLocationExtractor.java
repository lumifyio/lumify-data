package com.altamiracorp.reddawn.location;

import com.altamiracorp.reddawn.model.termMention.TermMention;
import com.altamiracorp.reddawn.ucd.artifact.Artifact;
import org.apache.hadoop.mapreduce.Mapper;

import java.io.IOException;
import java.util.List;

public interface ArtifactLocationExtractor {
    void setup(Mapper.Context context) throws IOException;

    void extract(Artifact artifact, List<TermMention> termMentions) throws Exception;
}
