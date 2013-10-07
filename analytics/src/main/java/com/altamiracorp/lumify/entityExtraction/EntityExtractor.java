package com.altamiracorp.lumify.entityExtraction;

import java.io.IOException;
import java.util.List;

import org.apache.hadoop.mapreduce.Mapper;

import com.altamiracorp.lumify.core.user.User;
import com.altamiracorp.lumify.ucd.artifact.Artifact;

public abstract class EntityExtractor {
    abstract void setup(Mapper.Context context, User user) throws IOException;

    abstract List<ExtractedEntity> extract(Artifact artifact, String text) throws Exception;
}
