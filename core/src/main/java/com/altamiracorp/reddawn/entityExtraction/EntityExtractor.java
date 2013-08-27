package com.altamiracorp.reddawn.entityExtraction;

import com.altamiracorp.reddawn.model.termMention.TermMention;
import com.altamiracorp.reddawn.ucd.artifact.Artifact;
import org.apache.hadoop.mapreduce.Mapper;

import java.io.IOException;
import java.util.List;

public abstract class EntityExtractor {
    abstract void setup(Mapper.Context context) throws IOException;

    abstract List<TermMention> extract(Artifact artifact, String text) throws Exception;
}
