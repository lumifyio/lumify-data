package com.altamiracorp.reddawn.search;

import com.altamiracorp.reddawn.ucd.models.Artifact;
import com.altamiracorp.reddawn.ucd.models.ArtifactKey;
import org.apache.hadoop.mapreduce.Mapper;

import java.io.IOException;
import java.util.Collection;

public interface SearchProvider {
    void setup(Mapper.Context context) throws Exception;

    void add(Artifact artifact) throws Exception;

    Collection<ArtifactKey> searchArtifacts(String query) throws Exception;
}
