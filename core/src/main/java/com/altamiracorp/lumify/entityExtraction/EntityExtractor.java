package com.altamiracorp.lumify.entityExtraction;

import com.altamiracorp.lumify.ucd.artifact.Artifact;
import org.apache.hadoop.mapreduce.Mapper;

import java.io.IOException;
import java.util.List;

public abstract class EntityExtractor {
    abstract void setup(Mapper.Context context) throws IOException;

    abstract List<ExtractedEntity> extract(Artifact artifact, String text) throws Exception;
}
