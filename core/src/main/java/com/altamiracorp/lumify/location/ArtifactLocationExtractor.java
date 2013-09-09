package com.altamiracorp.lumify.location;

import com.altamiracorp.lumify.model.termMention.TermMention;
import com.altamiracorp.lumify.ucd.artifact.Artifact;
import org.apache.hadoop.mapreduce.Mapper;

import java.io.IOException;
import java.util.List;

public interface ArtifactLocationExtractor {
    void setup(Mapper.Context context) throws IOException;

    void extract(Artifact artifact, List<TermMention> termMentions) throws Exception;
}
