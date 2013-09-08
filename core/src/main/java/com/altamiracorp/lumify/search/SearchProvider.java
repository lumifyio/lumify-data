package com.altamiracorp.lumify.search;

import com.altamiracorp.lumify.ucd.artifact.Artifact;
import org.apache.hadoop.mapreduce.Mapper;

import java.util.Collection;
import java.util.Properties;

public interface SearchProvider {
    void setup(Properties props);

    void setup(Mapper.Context context) throws Exception;

    void teardown() throws Exception;

    void add(Artifact artifact) throws Exception;

    Collection<ArtifactSearchResult> searchArtifacts(String query) throws Exception;

    void deleteIndex();

    void initializeIndex();
}
